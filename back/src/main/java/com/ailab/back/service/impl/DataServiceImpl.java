package com.ailab.back.service.impl;

import com.ailab.back.service.DataService;
import com.ailab.back.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据备份/恢复服务。
 *
 * <p>关键修复：原实现用 {@code ClassLoader.getResource("").getPath()} 定位备份目录，
 * 在打成 jar 运行时会得到 {@code ...jar!/BOOT-INF/classes!/db} 这种非法路径，
 * 导致备份/列表/恢复全部失败。现改为：
 * <ul>
 *   <li>优先使用配置项 {@code db.backup.dir}（绝对路径）；</li>
 *   <li>未配置时使用“应用启动目录/db”，开发与生产都能稳定读写；</li>
 * </ul>
 */
@Slf4j
@Service
public class DataServiceImpl implements DataService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /** 备份目录（绝对路径或相对于启动目录），留空则使用 ./db */
    @Value("${db.backup.dir:db}")
    private String backupDir;

    @Value("${db.backup.date-format:yyyy-MM-dd HH-mm-ss}")
    private String backupDateFormat;

    /** mysqldump / mysql 命令路径，默认走环境变量 PATH */
    @Value("${db.backup.mysqldump:mysqldump}")
    private String mysqldumpCmd;

    @Value("${db.backup.mysql:mysql}")
    private String mysqlCmd;

    /** 备份单次执行超时时间（秒） */
    @Value("${db.backup.timeout-seconds:120}")
    private int timeoutSeconds;

    private static final Pattern BACKUP_FILE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\\.sql$");

    // ====================== 备份 ======================
    @Override
    public ResultVO<?> backupData() {
        try {
            DbInfo db = parseDbInfo();
            if (db == null) {
                return ResultVO.error("解析数据库连接信息失败，请检查 spring.datasource.url 配置");
            }
            File dir = ensureBackupDir();
            String fileName = new SimpleDateFormat(backupDateFormat).format(new Date()) + ".sql";
            File backupFile = new File(dir, fileName);

            List<String> cmd = new ArrayList<>(List.of(
                    mysqldumpCmd,
                    "-h", db.host,
                    "-P", db.port,
                    "-u", dbUsername,
                    "--single-transaction",
                    "--default-character-set=utf8mb4",
                    "--databases", db.name,
                    "--result-file=" + backupFile.getAbsolutePath()
            ));
            CmdResult result = run(cmd, dbPassword);
            if (result.exitCode != 0) {
                return ResultVO.error("数据库备份失败，退出码：" + result.exitCode
                        + "\n排查方向：1.mysqldump 是否配置环境变量；2.数据库账号密码是否正确；3.备份目录是否有写权限"
                        + (result.output.isBlank() ? "" : "\n详情：" + result.output));
            }
            if (!backupFile.exists() || backupFile.length() == 0) {
                return ResultVO.error("数据库备份失败：备份文件为空，请检查 mysqldump 权限");
            }
            return ResultVO.success("数据库备份成功，文件路径：" + backupFile.getAbsolutePath());
        } catch (IOException e) {
            return ResultVO.error("IO异常：" + e.getMessage()
                    + "\n常见原因：mysqldump 未配置到环境变量，或备份目录无写入权限");
        } catch (Exception e) {
            log.error("备份失败", e);
            return ResultVO.error("备份失败：" + e.getMessage());
        }
    }

    // ====================== 恢复 ======================
    @Override
    public ResultVO<?> recoverData(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                return ResultVO.error("文件名不能为空");
            }
            if (!BACKUP_FILE_PATTERN.matcher(filename).matches()) {
                return ResultVO.error("文件名不符合规范（格式：yyyy-MM-dd HH-mm-ss.sql）");
            }
            File backupFile = new File(ensureBackupDir(), filename);
            if (!backupFile.exists() || !backupFile.isFile()) {
                return ResultVO.error("备份文件不存在：" + backupFile.getAbsolutePath());
            }
            DbInfo db = parseDbInfo();
            if (db == null) {
                return ResultVO.error("解析数据库连接信息失败，请检查 spring.datasource.url 配置");
            }

            // 通过标准输入导入 SQL，避免 -e "source xxx" 在带空格路径下失效
            List<String> cmd = List.of(
                    mysqlCmd,
                    "-h", db.host,
                    "-P", db.port,
                    "-u", dbUsername,
                    "--default-character-set=utf8mb4",
                    db.name
            );
            Process process = startProcess(cmd, dbPassword);
            try (InputStream in = Files.newInputStream(backupFile.toPath());
                 var out = process.getOutputStream()) {
                in.transferTo(out);
            }
            String output = readOutput(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResultVO.error("数据库恢复失败，退出码：" + exitCode
                        + "\n排查方向：1.mysql 是否配置环境变量；2.文件是否损坏；3.数据库账号是否有写入权限"
                        + (output.isBlank() ? "" : "\n详情：" + output));
            }
            return ResultVO.success("数据库恢复成功，恢复文件：" + backupFile.getAbsolutePath());
        } catch (IOException e) {
            return ResultVO.error("IO异常：" + e.getMessage() + "\n常见原因：mysql 未配置到环境变量，或文件无读取权限");
        } catch (Exception e) {
            log.error("恢复失败", e);
            return ResultVO.error("恢复失败：" + e.getMessage());
        }
    }

    // ====================== 删除备份 ======================
    @Override
    public ResultVO<?> deleteData(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                return ResultVO.error("文件名不能为空");
            }
            if (!BACKUP_FILE_PATTERN.matcher(filename).matches()) {
                return ResultVO.error("文件名不符合规范（格式：yyyy-MM-dd HH-mm-ss.sql）");
            }
            File backupFile = new File(ensureBackupDir(), filename);
            if (!backupFile.exists() || !backupFile.isFile()) {
                return ResultVO.error("备份文件不存在：" + backupFile.getAbsolutePath());
            }
            if (backupFile.delete()) {
                return ResultVO.success("备份文件删除成功：" + filename);
            }
            return ResultVO.error("备份文件删除失败，可能是文件被占用或无删除权限");
        } catch (Exception e) {
            return ResultVO.error("删除失败：" + e.getMessage());
        }
    }

    // ====================== 文件列表 ======================
    @Override
    public ResultVO<?> getFileList() {
        try {
            File dir = ensureBackupDir();
            File[] files = dir.listFiles((d, name) -> BACKUP_FILE_PATTERN.matcher(name).matches());
            List<Map<String, Object>> fileList = new ArrayList<>();
            if (files != null) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                for (File file : files) {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("fileName", file.getName());
                    info.put("fileSize", file.length());
                    info.put("fileSizeStr", formatFileSize(file.length()));
                    info.put("createTime", new Date(file.lastModified()));
                    fileList.add(info);
                }
            }
            return ResultVO.success().setData(fileList);
        } catch (Exception e) {
            return ResultVO.error("获取备份文件列表失败：" + e.getMessage());
        }
    }

    // ====================== 辅助 ======================

    private File ensureBackupDir() throws IOException {
        String dir = (backupDir == null || backupDir.isBlank()) ? "db" : backupDir.trim();
        if (dir.startsWith("file:")) {
            dir = dir.substring("file:".length());
        }
        Path path = Paths.get(dir).toAbsolutePath().normalize();
        File f = path.toFile();
        if (!f.exists() && !f.mkdirs()) {
            throw new IOException("创建备份目录失败：" + f.getAbsolutePath());
        }
        return f;
    }

    private record DbInfo(String host, String port, String name) {
    }

    private DbInfo parseDbInfo() {
        try {
            Matcher m = Pattern.compile("jdbc:mysql://([^/:?]+)(?::(\\d+))?/([^?;]+)").matcher(dbUrl);
            if (!m.find()) {
                return null;
            }
            String host = m.group(1);
            String port = m.group(2) == null ? "3306" : m.group(2);
            String name = m.group(3);
            return new DbInfo(host, port, name);
        } catch (Exception e) {
            return null;
        }
    }

    private record CmdResult(int exitCode, String output) {
    }

    private Process startProcess(List<String> cmd, String password) throws IOException {
        // 使用 MYSQL_PWD 传递密码，避免密码出现在进程命令行（ps 可见）以及 -p 与密码粘连问题
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        if (password != null) {
            env.put("MYSQL_PWD", password);
        }
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private CmdResult run(List<String> cmd, String password) throws IOException, InterruptedException {
        Process process = startProcess(cmd, password);
        String output = readOutput(process);
        boolean finished = process.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("命令执行超时（" + timeoutSeconds + "秒）：" + cmd.get(0));
        }
        return new CmdResult(process.exitValue(), output);
    }

    private String readOutput(Process process) throws IOException {
        try (InputStream in = process.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}

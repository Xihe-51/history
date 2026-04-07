package com.ailab.back.service.impl;

import com.ailab.back.service.DataService;
import com.ailab.back.vo.ResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataServiceImpl implements DataService {

    // ===== 从YAML读取配置 =====
    // 数据源配置
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    // 备份配置
    @Value("${db.backup.save-path}")
    private String backupSavePath;
    @Value("${db.backup.date-format}")
    private String backupDateFormat;

    // 正则表达式：匹配符合命名规范的备份文件名（yyyy-MM-dd HH-mm-ss.sql）
    private static final Pattern BACKUP_FILE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\\.sql$");

    // ====================== 已实现的备份方法 ======================
    @Override
    public ResultVO<?> backupData() {
        try {
            // 1. 解析数据库URL，提取数据库名、主机、端口
            Pattern dbNamePattern = Pattern.compile("jdbc:mysql://.*?/(.*?)\\?");
            Matcher dbNameMatcher = dbNamePattern.matcher(dbUrl);
            String dbName = "";
            if (dbNameMatcher.find()) {
                dbName = dbNameMatcher.group(1);
            } else {
                return ResultVO.error("解析数据库名称失败，请检查spring.datasource.url配置");
            }

            String host = "localhost";
            String port = "3306";
            Pattern hostPortPattern = Pattern.compile("jdbc:mysql://(.*?):(\\d+)/");
            Matcher hpMatcher = hostPortPattern.matcher(dbUrl);
            if (hpMatcher.find()) {
                host = hpMatcher.group(1);
                port = hpMatcher.group(2);
            }

            // 2. 生成带时间戳的备份文件名
            SimpleDateFormat sdf = new SimpleDateFormat(backupDateFormat);
            String currentTime = sdf.format(new Date());
            String fileName = currentTime + ".sql";

            // 3. 构建完整的备份文件路径
            String resourcePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();

            resourcePath = java.net.URLDecoder.decode(resourcePath, "UTF-8");
            File backupDir = new File(resourcePath + backupSavePath);
            if (!backupDir.exists()) {
                boolean mkdirSuccess = backupDir.mkdirs();
                if (!mkdirSuccess) {
                    return ResultVO.error("创建备份目录失败：" + backupDir.getAbsolutePath());
                }
            }
            String backupFilePath = backupDir.getAbsolutePath() + File.separator + fileName;
            // 4. 执行备份命令
            String[] cmd = new String[]{
                    "mysqldump",
                    "-h" + host,
                    "-P" + port,
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--databases", dbName,
                    "--result-file=" + backupFilePath
            };

            Process process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return ResultVO.success("数据库备份成功，文件路径：" + backupFilePath);
            } else {
                return ResultVO.error("数据库备份失败，退出码：" + exitCode +
                        "\n排查方向：1.mysqldump是否配置环境变量；2.数据库账号密码是否正确；3.目录是否有读写权限");
            }

        } catch (IOException e) {
            return ResultVO.error("IO异常：" + e.getMessage() + "\n常见原因：mysqldump未配置到环境变量，或备份目录无写入权限");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultVO.error("备份进程被中断：" + e.getMessage());
        } catch (Exception e) {
            return ResultVO.error("备份失败：" + e.getMessage());
        }
    }

    // ====================== 新增：恢复数据库 ======================
    @Override
    public ResultVO<?> recoverData(String filename) {
        try {
            // 1. 参数校验
            if (filename == null || filename.trim().isEmpty()) {
                return ResultVO.error("文件名不能为空");
            }
            if (!BACKUP_FILE_PATTERN.matcher(filename).matches()) {
                return ResultVO.error("文件名不符合规范（格式：yyyy-MM-dd HH-mm-ss.sql）");
            }

            // 2. 拼接备份文件完整路径
            String resourcePath = this.getClass().getClassLoader().getResource("").getPath();
            resourcePath = java.net.URLDecoder.decode(resourcePath, "UTF-8");
            File backupFile = new File(resourcePath + backupSavePath + File.separator + filename);

            // 3. 检查文件是否存在
            if (!backupFile.exists() || !backupFile.isFile()) {
                return ResultVO.error("备份文件不存在：" + backupFile.getAbsolutePath());
            }

            // 4. 解析数据库连接信息
            Pattern dbNamePattern = Pattern.compile("jdbc:mysql://.*?/(.*?)\\?");
            Matcher dbNameMatcher = dbNamePattern.matcher(dbUrl);
            String dbName = "";
            if (dbNameMatcher.find()) {
                dbName = dbNameMatcher.group(1);
            } else {
                return ResultVO.error("解析数据库名称失败，请检查spring.datasource.url配置");
            }

            String host = "localhost";
            String port = "3306";
            Pattern hostPortPattern = Pattern.compile("jdbc:mysql://(.*?):(\\d+)/");
            Matcher hpMatcher = hostPortPattern.matcher(dbUrl);
            if (hpMatcher.find()) {
                host = hpMatcher.group(1);
                port = hpMatcher.group(2);
            }

            // 5. 构建恢复命令（mysql命令执行sql文件）
            String[] cmd = new String[]{
                    "mysql",
                    "-h" + host,
                    "-P" + port,
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    dbName,
                    "-e", "source " + backupFile.getAbsolutePath().replace("\\", "\\\\")
            };

            // 6. 执行恢复命令
            Process process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return ResultVO.success("数据库恢复成功，恢复文件：" + backupFile.getAbsolutePath());
            } else {
                return ResultVO.error("数据库恢复失败，退出码：" + exitCode +
                        "\n排查方向：1.mysql是否配置环境变量；2.文件是否损坏；3.数据库账号是否有写入权限");
            }

        } catch (IOException e) {
            return ResultVO.error("IO异常：" + e.getMessage() + "\n常见原因：mysql未配置到环境变量，或文件无读取权限");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultVO.error("恢复进程被中断：" + e.getMessage());
        } catch (Exception e) {
            return ResultVO.error("恢复失败：" + e.getMessage());
        }
    }

    // ====================== 新增：删除备份文件 ======================
    @Override
    public ResultVO<?> deleteData(String filename) {
        try {
            // 1. 参数校验
            if (filename == null || filename.trim().isEmpty()) {
                return ResultVO.error("文件名不能为空");
            }

            // 2. 拼接文件完整路径
            String resourcePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
            resourcePath = java.net.URLDecoder.decode(resourcePath, StandardCharsets.UTF_8);
            File backupFile = new File(resourcePath + backupSavePath + File.separator + filename);

            // 3. 检查文件是否存在
            if (!backupFile.exists() || !backupFile.isFile()) {
                return ResultVO.error("备份文件不存在：" + backupFile.getAbsolutePath());
            }

            // 4. 删除文件
            boolean deleteSuccess = backupFile.delete();
            if (deleteSuccess) {
                return ResultVO.success("备份文件删除成功：" + filename);
            } else {
                return ResultVO.error("备份文件删除失败，可能是文件被占用或无删除权限");
            }

        } catch (Exception e) {
            return ResultVO.error("删除失败：" + e.getMessage());
        }
    }

    // ====================== 获取文件列表 ======================
    @Override
    public ResultVO<?> getFileList() {
        try {
            // 1. 获取备份目录路径
            String resourcePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
            resourcePath = java.net.URLDecoder.decode(resourcePath, StandardCharsets.UTF_8);
            File backupDir = new File(resourcePath + backupSavePath);

            // 2. 检查目录是否存在，不存在则创建并返回空列表
            if (!backupDir.exists()) {
                backupDir.mkdirs();
                return ResultVO.success(Collections.emptyList().toString());
            }

            // 3. 遍历目录，筛选符合命名规范的sql文件
            File[] files = backupDir.listFiles((dir, name) -> BACKUP_FILE_PATTERN.matcher(name).matches());
            if (files == null || files.length == 0) {
                return ResultVO.success(Collections.emptyList().toString());
            }

            // 4. 封装文件信息（文件名、文件大小（字节）、文件大小（友好显示））
            List<Map<String, Object>> fileList = new ArrayList<>();
            for (File file : files) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", file.getName());
                fileInfo.put("fileSize", file.length()); // 原始大小（字节）
                fileInfo.put("fileSizeStr", formatFileSize(file.length())); // 友好显示（如1.2MB）
                fileInfo.put("createTime", new Date(file.lastModified())); // 文件最后修改时间（近似创建时间）
                fileList.add(fileInfo);
            }

            // 5. 按文件创建时间倒序排序（最新的备份在最前面）
            fileList.sort((o1, o2) -> {
                Date time1 = (Date) o1.get("createTime");
                Date time2 = (Date) o2.get("createTime");
                return time2.compareTo(time1);
            });

            return ResultVO.success().setData(fileList);

        } catch (Exception e) {
            return ResultVO.error("获取备份文件列表失败：" + e.getMessage());
        }
    }

    // 辅助方法：格式化文件大小（字节转友好显示）
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
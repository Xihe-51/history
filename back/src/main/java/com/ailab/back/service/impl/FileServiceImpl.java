package com.ailab.back.service.impl;

import com.ailab.back.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务。
 *
 * <p>安全修复：
 * <ul>
 *   <li>按 resourceType 白名单校验扩展名，禁止上传 .html/.jsp/.svg 等可执行或可脚本的内容；</li>
 *   <li>校验 Content-Type，双重把关；</li>
 *   <li>存储目录统一到 upload 目录，不再写入前端源码目录。</li>
 * </ul>
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${image.upload.path}")
    private String imageUploadPath;

    @Value("${image.access.prefix:/images/}")
    private String imageAccessPrefix;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "ogg", "mov", "m4v");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "m4a", "aac");

    @Override
    public String uploadFile(MultipartFile file, String resourceType) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        String type = resourceType == null ? "图片" : resourceType.trim();
        Set<String> allowed;
        switch (type) {
            case "图片" -> allowed = IMAGE_EXTENSIONS;
            case "视频" -> allowed = VIDEO_EXTENSIONS;
            case "音频" -> allowed = AUDIO_EXTENSIONS;
            default -> throw new IllegalArgumentException("资源类型仅支持：图片/视频/音频");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名称不合法，缺少后缀");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!allowed.contains(suffix)) {
            throw new IllegalArgumentException("不支持的文件类型：." + suffix
                    + "，" + type + "仅支持：" + String.join("/", allowed));
        }

        // Content-Type 二次校验（部分浏览器对 webp 等会给空值，空则跳过）
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String major = contentType.substring(0, Math.min(contentType.length(), 5));
            if ("图片".equals(type) && !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("文件内容不是图片");
            }
            if ("视频".equals(type) && !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("文件内容不是视频");
            }
            if ("音频".equals(type) && !contentType.startsWith("audio/")) {
                throw new IllegalArgumentException("文件内容不是音频");
            }
        }

        File saveDir = resolveUploadDir();
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            throw new IOException("创建文件存储目录失败：" + saveDir.getAbsolutePath());
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        File saveFile = new File(saveDir, newFileName);
        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            throw new IOException("文件保存失败：" + e.getMessage(), e);
        }
        log.info("文件上传成功: {} -> {}", originalFilename, saveFile.getAbsolutePath());

        String prefix = imageAccessPrefix.endsWith("/") ? imageAccessPrefix : imageAccessPrefix + "/";
        return prefix + newFileName;
    }

    /** 解析上传目录：去掉 file: 前缀并转为绝对路径 */
    private File resolveUploadDir() {
        String p = imageUploadPath == null ? "" : imageUploadPath.trim();
        if (p.startsWith("file:")) {
            p = p.substring("file:".length());
        }
        Path path = Paths.get(p).toAbsolutePath().normalize();
        return path.toFile();
    }
}

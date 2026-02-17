package com.ailab.back.service.impl;

import com.ailab.back.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    // 直接从yml配置中注入文件上传物理路径
    @Value("${image.upload.path}")
    private String imageUploadPath;

    // 直接从yml配置中注入前端访问前缀
    @Value("${image.access.prefix}")
    private String imageAccessPrefix;

    @Override
    public String uploadFile(MultipartFile file, String resourceType) throws Exception {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 2. 校验资源类型（仅允许图片/视频）
        if (!"图片".equals(resourceType) && !"视频".equals(resourceType)) {
            throw new IllegalArgumentException("资源类型仅支持：图片/视频");
        }

        // 3. 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名称不合法，无后缀");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 4. 生成UUID新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 5. 处理存储路径（去除file:前缀，拼接完整路径）
        String uploadPath = imageUploadPath.replace("file:", "");
        File saveDir = new File(uploadPath);
        // 确保目录存在，不存在则创建
        if (!saveDir.exists()) {
            boolean mkdirs = saveDir.mkdirs();
            if (!mkdirs) {
                throw new IOException("创建文件存储目录失败：" + uploadPath);
            }
        }

        // 6. 拼接最终存储文件路径
        File saveFile = new File(saveDir, newFileName);

        // 7. 保存文件到指定路径
        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            throw new IOException("文件保存失败：" + e.getMessage(), e);
        }

        // 8. 拼接前端访问路径（访问前缀 + 新文件名）
        return imageAccessPrefix + newFileName;
    }
}
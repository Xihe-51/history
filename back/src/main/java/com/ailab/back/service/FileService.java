package com.ailab.back.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传多媒体文件（图片/视频）
     * @param file 上传的文件
     * @param resourceType 资源类型（图片/视频）
     * @return 前端可访问的文件路径
     * @throws Exception 上传异常
     */
    String uploadFile(MultipartFile file, String resourceType) throws Exception;
}
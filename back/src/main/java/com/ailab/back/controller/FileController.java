package com.ailab.back.controller;

import com.ailab.back.service.FileService;
import com.ailab.back.vo.ResultVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传多媒体文件接口
     * @param file 上传的文件（图片/视频）
     * @param resourceType 资源类型说明（图片/视频）
     * @return 包含访问路径的统一响应
     */
    @PostMapping("/upload")
    public ResultVO<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("resourceType") String resourceType) {
        try {
            // 调用服务层上传文件，获取访问路径
            String accessPath = fileService.uploadFile(file, resourceType);
            return ResultVO.<String>success().setData(accessPath);
        } catch (IllegalArgumentException e) {
            // 参数校验失败
            return ResultVO.error(e.getMessage());
        } catch (Exception e) {
            // 其他异常
            return ResultVO.error("文件上传失败：" + e.getMessage());
        }
    }
}

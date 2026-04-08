package com.ailab.back.service;

import com.ailab.back.vo.ResultIM;
import org.springframework.web.multipart.MultipartFile;
public interface ContentService {
    ResultIM importResult(MultipartFile file, String dataType, Long dynastyId);
}

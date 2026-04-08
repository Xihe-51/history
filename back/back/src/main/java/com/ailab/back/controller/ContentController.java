package com.ailab.back.controller;

import com.ailab.back.service.ContentService;
import com.ailab.back.vo.ResultIM;
import com.ailab.back.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    //导入excel
    @PostMapping("/import")
    public ResultVO<?>importExcel(
            @RequestParam("file")MultipartFile file,
            @RequestParam("dataType")String dataType,
            @RequestParam(value = "dynastyId",required = false)Long dynastyId) {
        // “||” 先判断null，在判断空字符
        if(file==null||file.isEmpty()){
            return ResultVO.error("文件不能为空");
        }
        if(dataType==null||dataType.isEmpty()){
            return ResultVO.error("dataType不能为空");
        }
        //调用读excel的逻辑实现类方法， 最终结果添加到ResultVo， 返回给前端
        ResultIM resultIM=contentService.importResult(file,dataType,dynastyId);
        return ResultVO.success(resultIM.builMsg()).setData(resultIM);
    }
}

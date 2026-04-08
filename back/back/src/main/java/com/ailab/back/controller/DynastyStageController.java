package com.ailab.back.controller;

import com.ailab.back.Entity.DynastyStage;
import com.ailab.back.service.DynastyStageService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dynasty-stage")
public class DynastyStageController {
    @Autowired
    private DynastyStageService dynastyStageService;

    @GetMapping("/page")
    public ResultVO<Page<DynastyStage>>getDynastyStagePage(
            @RequestParam(defaultValue = "1")Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize,
            @RequestParam Long dynastyId,
            @RequestParam(required = false)String stageName
    ) {
      return dynastyStageService.getDynastyStagePage(pageNum, pageSize, dynastyId, stageName);
    }
    @GetMapping("/{id}")
    public ResultVO<DynastyStage> getDynastyStageDetail(@PathVariable Long id) {
        return dynastyStageService.getDynastyStageDetail(id);
    }
    @PostMapping("/add")
    public ResultVO<?>add(@RequestBody DynastyStage dynastyStage) {
        return dynastyStageService.addDynastyStage(dynastyStage);
    }
    @PutMapping("/update")
    public ResultVO<?> update(@RequestBody DynastyStage dynastyStage) {
        return dynastyStageService.updateDynastyStage(dynastyStage);
    }
    @DeleteMapping("/delete/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        return dynastyStageService.deleteDynastyStage(id);
    }
    @DeleteMapping("/delete/batch")
    public ResultVO<?> deleteBatch(@RequestBody List<Long> ids) {
        return dynastyStageService.deleteDynastyBatch(ids);
    }
}

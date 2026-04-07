package com.ailab.back.controller;

import com.ailab.back.Entity.Allusion;
import com.ailab.back.service.AllusionService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allusion")
public class AllusionController {
    @Autowired
    private AllusionService allusionService;
    @GetMapping("/page")
    public ResultVO<Page<Allusion>>getAllusionPage(
            @RequestParam(defaultValue = "1")Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize,
            @RequestParam(required = false)String allusionName,
            @RequestParam(required = false)String source,
            @RequestParam(required = false)String themeType
    ){
        return allusionService.getAllusionPage(pageNum,pageSize,allusionName,source,themeType);
    }
    @GetMapping("/{id}")
    public ResultVO<Allusion> getAllusionById(@PathVariable Long id){
        return allusionService.getAllusionDetail(id);
    }
    @PostMapping("/add")
    public ResultVO<?> addAllusion(@RequestBody Allusion allusion){
        return allusionService.addAllusion(allusion);
    }
    @PutMapping("/update")
    public ResultVO<?> updateAllusion(@RequestBody Allusion allusion){
        return allusionService.updateAllusion(allusion);
    }
    @DeleteMapping("/delete/{id}")
    public ResultVO<?> deleteAllusion(@PathVariable Long id){
        return allusionService.deleteAllusion(id);
    }
    @DeleteMapping("/delete/batch")
    public ResultVO<?> deleteAllusionBatch(@RequestBody List<Long> ids){
        return allusionService.deleteAllusionBatch(ids);
    }
}

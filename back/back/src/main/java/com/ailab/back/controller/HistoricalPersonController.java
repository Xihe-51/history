package com.ailab.back.controller;


import com.ailab.back.Entity.HistoricalPerson;
import com.ailab.back.service.HistoricalPersonService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historical-person")
public class HistoricalPersonController {
    @Autowired
    private HistoricalPersonService historicalPersonService;
    @GetMapping("/page")
    public ResultVO<Page<HistoricalPerson>>getPage(
            @RequestParam(defaultValue = "1")Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize,
            @RequestParam Long dynastyId,
            @RequestParam(required = false)String personName,
            @RequestParam(required = false)String identityType,
            @RequestParam(required = false)String tag
    )
    {
        return historicalPersonService.getHistoricalPersonPage(pageNum,pageSize,dynastyId,personName,identityType,tag);
    }
    @GetMapping("/{id}")
    public ResultVO<HistoricalPerson>getDetail(@PathVariable Long id){
        return historicalPersonService.getHistoricalDetail(id);
    }
    @PostMapping("/add")
    public ResultVO<HistoricalPerson>add(@RequestBody HistoricalPerson historicalPerson){
        return historicalPersonService.addHistoricalPerson(historicalPerson);
    }
    @PutMapping("/update")
    public ResultVO<Void>update(@RequestBody HistoricalPerson historicalPerson){
        return historicalPersonService.updateHistoricalPerson(historicalPerson);
    }
    @DeleteMapping("delete/{id}")
    public ResultVO<Void>deleteById(@PathVariable Long id){
        return historicalPersonService.deleteHistoricalPerson(id);
    }
    @DeleteMapping("/delete/batch")
    public ResultVO<Void>deleteBatch(@RequestBody List<Long> ids){
        return  historicalPersonService.deleteHistoricalPersonBatch(ids);
    }
}

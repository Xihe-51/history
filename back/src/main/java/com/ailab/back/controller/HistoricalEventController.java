package com.ailab.back.controller;


import com.ailab.back.Entity.HistoricalEvent;
import com.ailab.back.service.HistoricalEventService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historical-event")
public class HistoricalEventController {

    @Autowired
    private HistoricalEventService historicalEventService;

    @GetMapping("/page")

    public ResultVO<Page<HistoricalEvent>>getHistoricalEventPage(
            @RequestParam(defaultValue = "1")Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize,
            @RequestParam Long dynastyId,
            @RequestParam(required = false)Long stageId,
            @RequestParam(required = false)String eventName,
            @RequestParam(required = false)String eventType,
            @RequestParam(required = false)String yearRange
    )
    {
        return historicalEventService.getHistoricalEventsPage(pageNum, pageSize, dynastyId, stageId, eventName, eventType, yearRange);
    }
    @GetMapping("/{id}")
    public ResultVO<HistoricalEvent>getHistoricalEventDetail(@PathVariable Long id){
        return historicalEventService.getHistoricalEventDetail(id);
    }
    @PostMapping("/add")
    public ResultVO<?> addHistoricalEvent(@RequestBody HistoricalEvent historicalEvent){
        return historicalEventService.addHistoricalEvent(historicalEvent);
    }
    @PutMapping("/update")
    public ResultVO<?> updateHistoricalEvent(@RequestBody HistoricalEvent historicalEvent){
        return historicalEventService.updateHistoricalEvent(historicalEvent);
    }
    @DeleteMapping("/delete/{id}")
    public ResultVO<?> deleteHistoricalEvent(@PathVariable Long id){
        return historicalEventService.deleteHistoricalEvent(id);
    }
    @DeleteMapping("/delete/batch")
    public ResultVO<?> deleteHistoricalEvent(@RequestBody List<Long> historicalEvents){
        return historicalEventService.deleteBatchHistoricalEvent(historicalEvents);
    }
}

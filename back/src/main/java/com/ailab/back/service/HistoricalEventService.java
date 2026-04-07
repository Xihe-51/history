package com.ailab.back.service;

import com.ailab.back.Entity.HistoricalEvent;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HistoricalEventService extends IService<HistoricalEvent> {
    ResultVO<Page<HistoricalEvent>>getHistoricalEventsPage(Integer pageNum, Integer pageSize, Long dynastyId,Long stageId,String eventName,String eventType, String yearRange);
    ResultVO<HistoricalEvent>getHistoricalEventDetail(Long id);
    ResultVO<?>addHistoricalEvent(HistoricalEvent historicalEvent);
    ResultVO<?>updateHistoricalEvent(HistoricalEvent historicalEvent);
    ResultVO<?>deleteHistoricalEvent(Long id);
    ResultVO<?>deleteBatchHistoricalEvent(List<Long> ids);
}

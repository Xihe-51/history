package com.ailab.back.service.impl;

import com.ailab.back.Entity.HistoricalEvent;
import com.ailab.back.mapper.HistoricalEventMapper;
import com.ailab.back.service.HistoricalEventService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class HistoricalEventServiceImpl extends ServiceImpl<HistoricalEventMapper, HistoricalEvent> implements HistoricalEventService {
    @Autowired
    private HistoricalEventMapper historicalEventMapper;

    @Override
    public ResultVO<Page<HistoricalEvent>> getHistoricalEventsPage(
            Integer pageNum,
            Integer pageSize,
            Long dynastyId,
            Long stageId,
            String eventName,
            String eventType,
            String yearRange) {

        // 1. 初始化分页对象（处理空值，避免分页参数异常
        Page<HistoricalEvent> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<HistoricalEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(eventName!=null&&!eventName.isEmpty(),HistoricalEvent::getEventName,eventName);
        queryWrapper.eq(eventType!=null&&!eventType.isEmpty(),HistoricalEvent::getEventType,eventType);
        queryWrapper.eq(dynastyId!=null,HistoricalEvent::getDynastyId,dynastyId);
        queryWrapper.eq(stageId!=null,HistoricalEvent::getStageId,stageId);
        // 3. 年份范围处理
        if(yearRange != null&&yearRange.contains("--")) {
            String[] arr=yearRange.split("--");
            if(arr.length==2&&!arr[0].isEmpty()&&!arr[1].isEmpty()) {
                queryWrapper.between(HistoricalEvent::getEventYear, Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()));
            }
        }
        // 4. 执行分页查询
        Page<HistoricalEvent> resultPage = historicalEventMapper.selectPage(page, queryWrapper);

        // 5. 返回结果
        return ResultVO.<Page<HistoricalEvent>>success("操作成功").setData(resultPage);
    }

    @Override
    public ResultVO<HistoricalEvent> getHistoricalEventDetail(Long id) {
        HistoricalEvent historicalEvent = historicalEventMapper.selectById(id);
        if(historicalEvent==null){
            return ResultVO.<HistoricalEvent>error("数据不存在");
        }
        return ResultVO.<HistoricalEvent>success("操作成功").setData(historicalEvent);
    }

    @Override
    public ResultVO<?> addHistoricalEvent(HistoricalEvent historicalEvent) {
        historicalEventMapper.insert(historicalEvent);
        return ResultVO.success("添加成功");
    }

    @Override
    public ResultVO<?> updateHistoricalEvent(HistoricalEvent historicalEvent) {
       historicalEventMapper.updateById(historicalEvent);
       return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> deleteHistoricalEvent(Long id) {
        historicalEventMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteBatchHistoricalEvent(List<Long> ids) {
        historicalEventMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
}

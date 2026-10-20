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
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class HistoricalEventServiceImpl extends ServiceImpl<HistoricalEventMapper, HistoricalEvent> implements HistoricalEventService {

    @Autowired
    private HistoricalEventMapper historicalEventMapper;

    /**
     * 分页查询事件。
     *
     * @param yearRange 年份范围，支持 "-221~-207"、"-221--207"（兼容旧格式）两种写法；
     *                  也支持只写一端，如 "-221~" / "~1911"
     */
    @Override
    public ResultVO<Page<HistoricalEvent>> getHistoricalEventsPage(
            Integer pageNum, Integer pageSize, Long dynastyId, Long stageId,
            String eventName, String eventType, String yearRange) {

        Page<HistoricalEvent> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));

        LambdaQueryWrapper<HistoricalEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(eventName), HistoricalEvent::getEventName, trim(eventName));
        queryWrapper.eq(StringUtils.hasText(eventType), HistoricalEvent::getEventType, trim(eventType));
        queryWrapper.eq(dynastyId != null, HistoricalEvent::getDynastyId, dynastyId);
        queryWrapper.eq(stageId != null, HistoricalEvent::getStageId, stageId);

        // 年份区间：修复原代码按 "--" 分割导致 "-221~-207" 形式永远不生效的问题
        if (StringUtils.hasText(yearRange)) {
            String raw = yearRange.trim();
            String separator = raw.contains("~") ? "~" : (raw.contains("--") ? "--" : null);
            if (separator != null) {
                String[] arr = raw.split(java.util.regex.Pattern.quote(separator), -1);
                if (arr.length == 2) {
                    boolean hasMin = StringUtils.hasText(arr[0]);
                    boolean hasMax = StringUtils.hasText(arr[1]);
                    Integer min = parseYear(arr[0]);
                    Integer max = parseYear(arr[1]);
                    if (hasMin && hasMax) {
                        // 两端都写：严格按闭区间过滤（min > max 时同样视为无结果，而不是退化成单边查询）
                        queryWrapper.between(HistoricalEvent::getEventYear,
                                Math.min(min, max), Math.max(min, max));
                    } else if (hasMin) {
                        queryWrapper.ge(HistoricalEvent::getEventYear, min);
                    } else if (hasMax) {
                        queryWrapper.le(HistoricalEvent::getEventYear, max);
                    }
                }
            }
        }

        // 时间轴按年份正序更符合阅读习惯
        queryWrapper.orderByAsc(HistoricalEvent::getEventYear);

        Page<HistoricalEvent> result = historicalEventMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<HistoricalEvent>>success("操作成功").setData(result);
    }

    @Override
    public ResultVO<HistoricalEvent> getHistoricalEventDetail(Long id) {
        HistoricalEvent event = historicalEventMapper.selectById(id);
        if (event == null) {
            return ResultVO.<HistoricalEvent>error("事件不存在");
        }
        return ResultVO.<HistoricalEvent>success("操作成功").setData(event);
    }

    @Override
    public ResultVO<?> addHistoricalEvent(HistoricalEvent event) {
        if (!StringUtils.hasText(event.getEventName())) {
            return ResultVO.error("事件名称不能为空");
        }
        if (event.getDynastyId() == null) {
            return ResultVO.error("所属朝代不能为空");
        }
        if (event.getEventYear() == null) {
            return ResultVO.error("事件年份不能为空");
        }
        if (!StringUtils.hasText(event.getEventType())) {
            event.setEventType("其他");
        }
        event.setId(null);
        historicalEventMapper.insert(event);
        return ResultVO.success("添加成功").setData(event);
    }

    @Override
    public ResultVO<?> updateHistoricalEvent(HistoricalEvent event) {
        if (event.getId() == null) {
            return ResultVO.error("事件ID不能为空");
        }
        if (historicalEventMapper.selectById(event.getId()) == null) {
            return ResultVO.error("事件不存在");
        }
        historicalEventMapper.updateById(event);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> deleteHistoricalEvent(Long id) {
        if (historicalEventMapper.deleteById(id) == 0) {
            return ResultVO.error("事件不存在或已被删除");
        }
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteBatchHistoricalEvent(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultVO.error("请选择要删除的事件");
        }
        historicalEventMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }

    private static Integer parseYear(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("年份格式错误：" + text.trim() + "（应为整数，公元前用负数）");
        }
    }

    private static int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private static int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 || pageSize > 1000 ? 10 : pageSize;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}

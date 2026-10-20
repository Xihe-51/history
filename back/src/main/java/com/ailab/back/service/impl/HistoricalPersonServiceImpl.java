package com.ailab.back.service.impl;

import com.ailab.back.Entity.HistoricalPerson;
import com.ailab.back.mapper.HistoricalPersonMapper;
import com.ailab.back.service.HistoricalPersonService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class HistoricalPersonServiceImpl extends ServiceImpl<HistoricalPersonMapper, HistoricalPerson> implements HistoricalPersonService {

    @Autowired
    private HistoricalPersonMapper historicalPersonMapper;

    @Override
    public ResultVO<Page<HistoricalPerson>> getHistoricalPersonPage(
            Integer pageNum, Integer pageSize, Long dynastyId,
            String personName, String identityType, String tag) {

        Page<HistoricalPerson> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));

        LambdaQueryWrapper<HistoricalPerson> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dynastyId != null, HistoricalPerson::getDynastyId, dynastyId);
        queryWrapper.like(StringUtils.hasText(personName), HistoricalPerson::getPersonName, trim(personName));
        queryWrapper.eq(StringUtils.hasText(identityType), HistoricalPerson::getIdentityType, trim(identityType));
        // 参数化绑定，避免原字符串拼接带来的 SQL 注入
        queryWrapper.apply(StringUtils.hasText(tag), "JSON_CONTAINS(tags, JSON_QUOTE({0}))", trim(tag));
        // 按生卒年排序，便于时间跨期阅读
        queryWrapper.orderByAsc(HistoricalPerson::getBirthYear);

        Page<HistoricalPerson> result = historicalPersonMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<HistoricalPerson>>success("操作成功").setData(result);
    }

    @Override
    public ResultVO<HistoricalPerson> getHistoricalDetail(Long id) {
        HistoricalPerson person = historicalPersonMapper.selectById(id);
        if (person == null) {
            return ResultVO.<HistoricalPerson>error("人物不存在");
        }
        return ResultVO.<HistoricalPerson>success("操作成功").setData(person);
    }

    @Override
    public ResultVO<HistoricalPerson> addHistoricalPerson(HistoricalPerson person) {
        if (!StringUtils.hasText(person.getPersonName())) {
            return ResultVO.error("人物姓名不能为空");
        }
        if (person.getDynastyId() == null) {
            return ResultVO.error("所属朝代不能为空");
        }
        if (!StringUtils.hasText(person.getIdentityType())) {
            person.setIdentityType("其他");
        }
        if (person.getBirthYear() != null && person.getDeathYear() != null
                && person.getBirthYear() > person.getDeathYear()) {
            return ResultVO.error("出生年份不能晚于去世年份");
        }
        person.setId(null);
        historicalPersonMapper.insert(person);
        return ResultVO.<HistoricalPerson>success("新增成功").setData(person);
    }

    @Override
    public ResultVO<Void> updateHistoricalPerson(HistoricalPerson person) {
        if (person.getId() == null) {
            return ResultVO.error("人物ID不能为空");
        }
        if (historicalPersonMapper.selectById(person.getId()) == null) {
            return ResultVO.error("人物不存在");
        }
        if (person.getBirthYear() != null && person.getDeathYear() != null
                && person.getBirthYear() > person.getDeathYear()) {
            return ResultVO.error("出生年份不能晚于去世年份");
        }
        historicalPersonMapper.updateById(person);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<Void> deleteHistoricalPerson(Long id) {
        if (historicalPersonMapper.deleteById(id) == 0) {
            return ResultVO.error("人物不存在或已被删除");
        }
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<Void> deleteHistoricalPersonBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultVO.error("请选择要删除的人物");
        }
        historicalPersonMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
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

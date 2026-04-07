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
            Integer pageNum,
            Integer pageSize,
            Long dynastyId,
            String personName,
            String identityType,
            String tag
    ) {
        //创建分页对象
        Page<HistoricalPerson> page = new Page<>(pageNum, pageSize);
        //构建查询条件
        LambdaQueryWrapper<HistoricalPerson> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dynastyId!=null,HistoricalPerson::getDynastyId,dynastyId);
        queryWrapper.like(StringUtils.hasText(personName),HistoricalPerson::getPersonName,personName);
        queryWrapper.eq(StringUtils.hasText(identityType),HistoricalPerson::getIdentityType,identityType);
        queryWrapper.apply(tag!=null&&!tag.isEmpty(),"JSON_CONTAINS(tags,'\""+tag+"\"')");
        //执行分页查询
        Page<HistoricalPerson> pageResult = historicalPersonMapper.selectPage(page, queryWrapper);
        return  ResultVO.<Page<HistoricalPerson>>success("操作成功").setData(pageResult);
    }

    @Override
    public ResultVO<HistoricalPerson> getHistoricalDetail(Long id) {
        HistoricalPerson historicalPerson = historicalPersonMapper.selectById(id);
        return ResultVO.<HistoricalPerson>success("操作成功").setData(historicalPerson);
    }

    @Override
    public ResultVO<HistoricalPerson> addHistoricalPerson(HistoricalPerson historicalPerson) {
        historicalPersonMapper.insert(historicalPerson);
        return ResultVO.<HistoricalPerson>success("新增成功").setData(historicalPerson);
    }

    @Override
    public ResultVO<Void> updateHistoricalPerson(HistoricalPerson historicalPerson) {
        historicalPersonMapper.updateById(historicalPerson);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<Void> deleteHistoricalPerson(Long id) {
        historicalPersonMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<Void> deleteHistoricalPersonBatch(List<Long> ids) {
        historicalPersonMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
}

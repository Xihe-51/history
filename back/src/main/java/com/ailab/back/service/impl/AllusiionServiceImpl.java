package com.ailab.back.service.impl;

import com.ailab.back.Entity.Allusion;
import com.ailab.back.mapper.AllusionMapper;
import com.ailab.back.service.AllusionService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllusiionServiceImpl extends ServiceImpl<AllusionMapper, Allusion> implements AllusionService {
    @Autowired
    private AllusionMapper allusionMapper;
    @Override
    public ResultVO<Page<Allusion>> getAllusionPage(Integer pageNum, Integer pageSize, String allusionName, String source, String themeType) {
        Page<Allusion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Allusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(allusionName!=null&&allusionName.isEmpty(),Allusion::getAllusionName,allusionName);
        queryWrapper.like(source!=null&&source.isEmpty(),Allusion::getSource,source);
        queryWrapper.like(themeType!=null&&themeType.isEmpty(),Allusion::getThemeType,themeType);
        Page<Allusion> allusionResult = allusionMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<Allusion>>success("操作成功").setData(allusionResult);
    }

    @Override
    public ResultVO<Allusion> getAllusionDetail(Long id) {
        Allusion allusion = allusionMapper.selectById(id);
        return ResultVO.<Allusion>success("操作成功").setData(allusion);
    }

    @Override
    public ResultVO<?> addAllusion(Allusion allusion) {
        allusionMapper.insert(allusion);
        return ResultVO.success("新增成功").setData(allusion);
    }

    @Override
    public ResultVO<?> updateAllusion(Allusion allusion) {
        allusionMapper.updateById(allusion);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> deleteAllusion(Long id) {
        allusionMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteAllusionBatch(List<Long> ids) {
        allusionMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
}

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
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AllusiionServiceImpl extends ServiceImpl<AllusionMapper, Allusion> implements AllusionService {

    @Autowired
    private AllusionMapper allusionMapper;

    @Override
    public ResultVO<Page<Allusion>> getAllusionPage(Integer pageNum, Integer pageSize,
                                                    String allusionName, String source, String themeType) {
        Page<Allusion> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));

        LambdaQueryWrapper<Allusion> queryWrapper = new LambdaQueryWrapper<>();
        // 修复：原条件写成 isEmpty()，导致只有传入空串时才拼接条件，筛选永远失效
        queryWrapper.like(StringUtils.hasText(allusionName), Allusion::getAllusionName, trim(allusionName));
        queryWrapper.like(StringUtils.hasText(source), Allusion::getSource, trim(source));
        queryWrapper.eq(StringUtils.hasText(themeType), Allusion::getThemeType, trim(themeType));
        queryWrapper.orderByAsc(Allusion::getId);

        Page<Allusion> result = allusionMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<Allusion>>success("操作成功").setData(result);
    }

    @Override
    public ResultVO<Allusion> getAllusionDetail(Long id) {
        Allusion allusion = allusionMapper.selectById(id);
        if (allusion == null) {
            return ResultVO.<Allusion>error("典故不存在");
        }
        return ResultVO.<Allusion>success("操作成功").setData(allusion);
    }

    @Override
    public ResultVO<?> addAllusion(Allusion allusion) {
        if (!StringUtils.hasText(allusion.getAllusionName())) {
            return ResultVO.error("典故名称不能为空");
        }
        if (!StringUtils.hasText(allusion.getThemeType())) {
            allusion.setThemeType("其他");
        }
        allusion.setId(null);
        allusionMapper.insert(allusion);
        return ResultVO.success("新增成功").setData(allusion);
    }

    @Override
    public ResultVO<?> updateAllusion(Allusion allusion) {
        if (allusion.getId() == null) {
            return ResultVO.error("典故ID不能为空");
        }
        if (allusionMapper.selectById(allusion.getId()) == null) {
            return ResultVO.error("典故不存在");
        }
        allusionMapper.updateById(allusion);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> deleteAllusion(Long id) {
        if (allusionMapper.deleteById(id) == 0) {
            return ResultVO.error("典故不存在或已被删除");
        }
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteAllusionBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultVO.error("请选择要删除的典故");
        }
        allusionMapper.deleteBatchIds(ids);
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

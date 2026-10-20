package com.ailab.back.service.impl;

import com.ailab.back.Entity.Dynasty;
import com.ailab.back.mapper.DynastyMapper;
import com.ailab.back.service.DynastyService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DynastyServiceImpl extends ServiceImpl<DynastyMapper, Dynasty> implements DynastyService {

    @Autowired
    private DynastyMapper dynastyMapper;

    @Override
    public ResultVO<Page<Dynasty>> getDynastyPage(Integer pageNum, Integer pageSize,
                                                  String name, String capital, String founder) {
        Page<Dynasty> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));

        LambdaQueryWrapper<Dynasty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(name), Dynasty::getName, trim(name));
        queryWrapper.like(StringUtils.hasText(capital), Dynasty::getCapital, trim(capital));
        queryWrapper.like(StringUtils.hasText(founder), Dynasty::getFounder, trim(founder));
        queryWrapper.orderByAsc(Dynasty::getStartYear);

        Page<Dynasty> result = dynastyMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<Dynasty>>success("操作成功").setData(result);
    }

    @Override
    public ResultVO<Dynasty> getDynastyDetail(Long id) {
        Dynasty dynasty = dynastyMapper.selectById(id);
        if (dynasty == null) {
            return ResultVO.<Dynasty>error("朝代不存在");
        }
        return ResultVO.<Dynasty>success("操作成功").setData(dynasty);
    }

    @Override
    public ResultVO<?> addDynasty(Dynasty dynasty) {
        if (!StringUtils.hasText(dynasty.getName())) {
            return ResultVO.error("朝代名称不能为空");
        }
        if (dynasty.getStartYear() == null || dynasty.getEndYear() == null) {
            return ResultVO.error("起始年份和结束年份不能为空");
        }
        if (dynasty.getStartYear() > dynasty.getEndYear()) {
            return ResultVO.error("起始年份不能晚于结束年份");
        }
        dynasty.setId(null);
        dynastyMapper.insert(dynasty);
        return ResultVO.success("新增成功").setData(dynasty);
    }

    @Override
    public ResultVO<?> updateDynasty(Dynasty dynasty) {
        if (dynasty.getId() == null) {
            return ResultVO.error("朝代ID不能为空");
        }
        if (dynastyMapper.selectById(dynasty.getId()) == null) {
            return ResultVO.error("朝代不存在");
        }
        if (dynasty.getStartYear() != null && dynasty.getEndYear() != null
                && dynasty.getStartYear() > dynasty.getEndYear()) {
            return ResultVO.error("起始年份不能晚于结束年份");
        }
        dynastyMapper.updateById(dynasty);
        return ResultVO.success("更新成功");
    }

    /**
     * 删除朝代：外键配置为 ON DELETE CASCADE，
     * 会级联删除该朝代下的阶段、事件、人物（操作不可恢复，前端需二次确认）。
     */
    @Override
    public ResultVO<?> deleteDynasty(Long id) {
        if (dynastyMapper.deleteById(id) == 0) {
            return ResultVO.error("朝代不存在或已被删除");
        }
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<Dynasty> deleteDynastyBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultVO.error("请选择要删除的朝代");
        }
        dynastyMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }

    @Override
    public ResultVO<List<Dynasty>> getAllDynasty() {
        List<Dynasty> dynasties = dynastyMapper.selectList(
                new LambdaQueryWrapper<Dynasty>().orderByAsc(Dynasty::getStartYear));
        return ResultVO.<List<Dynasty>>success("操作成功").setData(dynasties);
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

package com.ailab.back.service.impl;

import com.ailab.back.Entity.DynastyStage;
import com.ailab.back.mapper.DynastyStageMapper;
import com.ailab.back.service.DynastyStageService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynastyStageServiceImpl extends ServiceImpl<DynastyStageMapper, DynastyStage> implements DynastyStageService {
    @Autowired
    private DynastyStageMapper dynastyStageMapper;
    @Override
    public ResultVO<Page<DynastyStage>> getDynastyStagePage(Integer pageNum, Integer pageSize, Long dynastyId, String stageName) {
        Page<DynastyStage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DynastyStage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DynastyStage::getDynastyId, dynastyId);
        queryWrapper.like(stageName!=null&&!stageName.isEmpty(),DynastyStage::getStageName,stageName);
        Page<DynastyStage>resultPage = dynastyStageMapper.selectPage(page, queryWrapper);
        return ResultVO.<Page<DynastyStage>>success("操作成功").setData(resultPage);
    }

    @Override
    public ResultVO<DynastyStage> getDynastyStageDetail(Long id) {
        DynastyStage dynastyStage = dynastyStageMapper.selectById(id);
        if(dynastyStage==null){
            return ResultVO.<DynastyStage>error("该数据不存在");
        }
        return ResultVO.<DynastyStage>success("操作成功").setData(dynastyStage);
    }

    @Override
    public ResultVO<?> updateDynastyStage(DynastyStage dynastyStage) {
        dynastyStageMapper.updateById(dynastyStage);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> addDynastyStage(DynastyStage dynastyStage) {
        dynastyStageMapper.insert(dynastyStage);
        return ResultVO.success("添加成功");
    }

    @Override
    public ResultVO<?> deleteDynastyStage(Long id) {
        dynastyStageMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteDynastyBatch(List<Long> ids) {
        dynastyStageMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
}

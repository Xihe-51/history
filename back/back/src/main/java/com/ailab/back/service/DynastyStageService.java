package com.ailab.back.service;

import com.ailab.back.Entity.DynastyStage;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DynastyStageService extends IService<DynastyStage> {
    ResultVO<Page<DynastyStage>>getDynastyStagePage(Integer pageNum, Integer pageSize,Long dynastyId,String stageName);
    ResultVO<DynastyStage> getDynastyStageDetail(Long id);
    ResultVO<?>updateDynastyStage(DynastyStage dynastyStage);
    ResultVO<?>addDynastyStage(DynastyStage dynastyStage);
    ResultVO<?>deleteDynastyStage(Long id);
    ResultVO<?>deleteDynastyBatch(List<Long> ids);
}

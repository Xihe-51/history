package com.ailab.back.service;

import com.ailab.back.Entity.Allusion;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface  AllusionService extends IService<Allusion> {
    ResultVO<Page<Allusion>>getAllusionPage(Integer pageNum, Integer pageSize,
                                            String allusionName,String source,String themeType);
    ResultVO<Allusion>getAllusionDetail(Long id);
    ResultVO<?>addAllusion(Allusion allusion);
    ResultVO<?>updateAllusion(Allusion allusion);
    ResultVO<?>deleteAllusion(Long id);
    ResultVO<?>deleteAllusionBatch(List<Long> ids);
}

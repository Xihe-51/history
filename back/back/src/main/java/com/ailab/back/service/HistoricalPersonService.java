package com.ailab.back.service;

import com.ailab.back.Entity.HistoricalPerson;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HistoricalPersonService extends IService<HistoricalPerson> {
    ResultVO<Page<HistoricalPerson>>getHistoricalPersonPage(
            Integer pageNum,
            Integer pageSize,
            Long dynastyId,
            String personName,
            String identityType,
            String tag
    );
    ResultVO<HistoricalPerson>getHistoricalDetail(Long id);
    ResultVO<HistoricalPerson>addHistoricalPerson(HistoricalPerson historicalPerson);
    ResultVO<Void>updateHistoricalPerson(HistoricalPerson historicalPerson);
    ResultVO<Void>deleteHistoricalPerson(Long id);
    ResultVO<Void>deleteHistoricalPersonBatch(List<Long> ids);
}

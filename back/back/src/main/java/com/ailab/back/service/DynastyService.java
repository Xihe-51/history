package com.ailab.back.service;

import com.ailab.back.Entity.Dynasty;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DynastyService extends IService<Dynasty>{
    //分页查询
    ResultVO<Page<Dynasty>> getDynastyPage(Integer pageNum, Integer pageSize,
                                  String name, String capital, String founder);
    //获取详细
    ResultVO<Dynasty> getDynastyDetail(Long id);
    //新增朝代
    ResultVO<?>addDynasty(Dynasty dynasty);
    //更新朝代
    ResultVO<?>updateDynasty(Dynasty dynasty);
    //删除朝代
    ResultVO<?>deleteDynasty(Long id);
    //批量删除朝代
    ResultVO<Dynasty>deleteDynastyBatch(List<Long> ids);
    //获取所有朝代
    ResultVO<List<Dynasty>>getAllDynasty();


}

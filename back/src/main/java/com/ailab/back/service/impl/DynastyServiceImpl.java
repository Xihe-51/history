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

import java.util.List;
@Service
public class DynastyServiceImpl extends ServiceImpl<DynastyMapper, Dynasty> implements DynastyService {
    //操作数据库
    @Autowired
    private DynastyMapper dynastyMapper;
    //分页查询
    @Override
    public ResultVO<Page<Dynasty>> getDynastyPage(Integer pageNum, Integer pageSize,
                                 String name, String capital, String founder) {

        // 创建分页对象
        Page<Dynasty> page = new Page<>(pageNum, pageSize);

        // 查询条件
        LambdaQueryWrapper<Dynasty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.isEmpty(), Dynasty::getName, name);
        queryWrapper.like(capital != null && !capital.isEmpty(), Dynasty::getCapital, capital);
        queryWrapper.like(founder != null && !founder.isEmpty(), Dynasty::getFounder, founder);

        // 执行分页查询
        Page<Dynasty> pageResult = dynastyMapper.selectPage(page, queryWrapper);

        // 返回统一格式
        return ResultVO.<Page<Dynasty>>success("操作成功").setData(pageResult);
    }

    //获取朝代详细
    @Override
    public ResultVO<Dynasty> getDynastyDetail(Long id) {
        Dynasty dynasty = dynastyMapper.selectById(id);
        return ResultVO.<Dynasty>success("操作成功").setData(dynasty);
    }
    //新增
    @Override
    public ResultVO<?> addDynasty(Dynasty dynasty) {
        dynastyMapper.insert(dynasty);
        return ResultVO.success("新增成功").setData(dynasty);
    }
    //更新
    @Override
    public ResultVO<?> updateDynasty(Dynasty dynasty) {
        dynastyMapper.updateById(dynasty);
        return ResultVO.success("更新成功");
    }
    //删除
    @Override
    public ResultVO<?> deleteDynasty(Long id) {
        dynastyMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }
    //批量删除
    @Override
    public ResultVO<Dynasty> deleteDynastyBatch(List<Long> ids) {
        dynastyMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
    //获取所有朝代
    @Override
    public ResultVO<List<Dynasty>> getAllDynasty() {
        List<Dynasty> dynastys = dynastyMapper.selectList(null);
        return ResultVO.<List<Dynasty>>success("操作成功").setData(dynastys);
    }
}

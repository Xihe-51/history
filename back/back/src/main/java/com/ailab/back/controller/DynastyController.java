package com.ailab.back.controller;

import com.ailab.back.Entity.Dynasty;
import com.ailab.back.service.DynastyService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//返回json
@RestController
//所有接口都是/dynasty开头
@RequestMapping("/dynasty")
public class DynastyController {
    //操作数据库
    @Autowired
    private DynastyService dynastyService;
    //分页查询
    @GetMapping("/page")
    public ResultVO<Page<Dynasty>>getDynastyPage(
            @RequestParam(defaultValue ="1")Integer pageNum,
            @RequestParam(defaultValue = "10")Integer pageSize,
            @RequestParam(required = false)String name,
            @RequestParam(required = false)String capital,
            @RequestParam(required = false)String founder
    ){
        return dynastyService.getDynastyPage(pageNum,pageSize,name,capital,founder);
    }
    //朝代详细
    @GetMapping("/{id}")
    public ResultVO<Dynasty> getDynastyDetail(@PathVariable Long id){
        return dynastyService.getDynastyDetail(id);
    }
    //新增
    @PostMapping("/add")
    public ResultVO<?> addDynasty(@RequestBody Dynasty dynasty){
        return dynastyService.addDynasty(dynasty);
    }
    //更新
    @PutMapping("/update")
    public ResultVO<?> updateDynasty(@RequestBody Dynasty dynasty){
        return dynastyService.updateDynasty(dynasty);
    }
    //删除
    @DeleteMapping("/delete/{id}")
    public ResultVO<?> deleteDynasty(@PathVariable Long id){
        return dynastyService.deleteDynasty(id);
    }
    //批量删除
    @DeleteMapping("/delete/batch")
    public ResultVO<?> deleteDynastyBatch(@RequestBody List<Long> ids){
        return dynastyService.deleteDynastyBatch(ids);
    }
    //获取所有
    @GetMapping("/list/all")
    public ResultVO<List<Dynasty>> getAllDynastyList(){
        return dynastyService.getAllDynasty();
    }
}

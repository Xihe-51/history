package com.ailab.back.mapper;

import com.ailab.back.Entity.Dynasty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
@Mapper
//mapper能被spring扫描
//继承BaseMapper有增删改查的功能
//不需要写xml文件，mybatis-plus自动生成sql
//简单的增删改查不需要xml文件，继承mybatisplus现有的增删改查
public interface DynastyMapper extends BaseMapper <Dynasty>{
}

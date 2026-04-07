package com.ailab.back.mapper;

import com.ailab.back.Entity.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//继承类型--数据库字段与实体类字段一一对应
public interface NoteMapper extends BaseMapper<Note> {
}

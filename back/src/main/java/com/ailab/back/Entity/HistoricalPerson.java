package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "historical_person", autoResultMap = true)
public class HistoricalPerson {
    // 人物ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属朝代ID
    private Long dynastyId;

    // 人物姓名
    private String personName;

    // 出生年份（公元前为负值）
    private Integer birthYear;

    // 去世年份（公元前为负值）
    private Integer deathYear;

    // 身份类型（帝王/将相/文人/科学家/其他）
    private String identityType;

    // 人物标签（JSON数组，如：["诗人","政治家"]）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags; // 若使用JSON解析可替换为JSONObject/JSONArray

    // 头像图片路径
    private String avatarPath;

    // 人物生平简介
    private String personBio;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

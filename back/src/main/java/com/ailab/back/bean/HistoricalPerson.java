package com.ailab.back.bean;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("historical_person")
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
    private String tags; // 若使用JSON解析可替换为JSONObject/JSONArray

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
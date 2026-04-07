package com.ailab.back.bean;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynasty_stage")
public class DynastyStage {
    // 阶段ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属朝代ID
    private Long dynastyId;

    // 阶段名称（如：盛唐、晚唐）
    private String stageName;

    // 阶段起始年份
    private Integer startYear;

    // 阶段结束年份
    private Integer endYear;

    // 阶段详细描述
    private String stageDesc;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
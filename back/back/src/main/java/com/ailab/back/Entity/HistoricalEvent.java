package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("historical_event")
public class HistoricalEvent {
    // 事件ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属朝代ID
    private Long dynastyId;

    // 所属朝代阶段ID（可选）
    private Long stageId;

    // 事件名称
    private String eventName;

    // 事件核心年份（公元前为负值）
    private Integer eventYear;

    // 事件背景描述
    private String eventBg;

    // 事件过程描述
    private String eventProcess;

    // 事件影响描述
    private String eventImpact;

    // 事件类型（政治/军事/文化/经济/社会/其他）
    private String eventType;

    // 事件配图路径
    private String eventPicPath;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
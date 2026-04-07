package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynasty")
public class Dynasty {
    // 朝代ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 朝代名称
    private String name;

    // 起始年份（公元前为负值）
    private Integer startYear;

    // 结束年份（公元前为负值）
    private Integer endYear;

    // 都城
    private String capital;

    // 开国者/建立者
    private String founder;

    // 朝代地图路径
    private String mapPath;

    // 朝代简介描述
    private String dynastyDesc;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
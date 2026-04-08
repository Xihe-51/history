package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("allusion")
public class Allusion {
    // 典故ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 典故名称
    private String allusionName;

    // 出处（古籍名称）
    private String source;

    // 原文
    private String originalText;

    // 释义
    private String paraphrase;

    // 寓意
    private String moral;

    // 主题类型（励志/权谋/友情/忠诚/智慧/其他）
    private String themeType;

    // 多媒体资源路径（图片/音频/视频）
    private String multimediaPath;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
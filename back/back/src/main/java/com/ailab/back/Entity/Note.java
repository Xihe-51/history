package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("note")
public class Note {
    // 笔记ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID
    private Long userId;

    // 笔记类型
    private String type;

    // 笔记标题
    private String title;

    // 富文本笔记内容
    private String content;

    // 笔记标签（JSON数组）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags; // 若使用JSON解析可替换为JSONObject/JSONArray

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
package com.ailab.back.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 趣味互动题库题目。
 * 题型：single 单选 / multiple 多选 / judge 判断；
 * 知识点来源：event 事件 / person 人物 / allusion 典故 / dynasty 朝代。
 */
@Data
@TableName("question")
public class Question {
    // 题目ID，主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 题型（single/multiple/judge）
    private String questionType;

    // 知识点来源（event/person/allusion/dynasty）
    private String topicType;

    // 题干
    private String content;

    // 选项数组（JSON 文本，判断题固定为 ["正确","错误"]）
    private String options;

    // 正确答案（单选存选项索引字母，多选存 "A,B"，判断题存 "正确"/"错误"）
    private String answer;

    // 答案解析
    private String analysis;

    // 难度（easy/medium/hard）
    private String difficulty;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

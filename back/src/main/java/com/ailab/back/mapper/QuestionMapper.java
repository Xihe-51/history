package com.ailab.back.mapper;

import com.ailab.back.Entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 建表（幂等）。用于本地/演示环境首次启动时自动创建题库表，避免手工执行 SQL。
     */
    @Update("CREATE TABLE IF NOT EXISTS `question` (" +
            "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键'," +
            "`question_type` VARCHAR(50) NOT NULL DEFAULT 'single' COMMENT '题型：single/multiple/judge'," +
            "`topic_type` VARCHAR(50) NOT NULL DEFAULT 'other' COMMENT '知识点来源：event/person/allusion/dynasty'," +
            "`content` VARCHAR(500) NOT NULL COMMENT '题干'," +
            "`options` JSON NULL COMMENT '选项数组（JSON）'," +
            "`answer` VARCHAR(200) NOT NULL COMMENT '正确答案'," +
            "`analysis` TEXT NULL COMMENT '答案解析'," +
            "`difficulty` VARCHAR(20) NOT NULL DEFAULT 'easy' COMMENT '难度：easy/medium/hard'," +
            "`created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "`updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "PRIMARY KEY (`id`)," +
            "INDEX `idx_question_topic` (`topic_type`)," +
            "INDEX `idx_question_difficulty` (`difficulty`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='趣味互动题库表'")
    void initTable();

    /**
     * 随机抽取题目：按知识点来源与难度过滤，答题时使用。
     */
    @Select("<script>" +
            "SELECT * FROM question" +
            "<where>" +
            "<if test='topicType != null and topicType != \"\"'> AND topic_type = #{topicType}</if>" +
            "<if test='difficulty != null and difficulty != \"\"'> AND difficulty = #{difficulty}</if>" +
            "</where>" +
            " ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Question> selectRandom(@Param("count") int count,
                                @Param("topicType") String topicType,
                                @Param("difficulty") String difficulty);
}

package com.ailab.back.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus字段自动填充配置
 * 自动填充创建时间和更新时间
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时填充字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时填充字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }
}
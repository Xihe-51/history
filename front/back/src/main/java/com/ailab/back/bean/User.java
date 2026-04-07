package com.ailab.back.bean;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    // 主键ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户名
    private String username;

    // 密码哈希
    private String passwordHash;

    // 邮箱
    private String email;

    // 头像路径
    private String avatarPath;

    // 角色（admin/user）
    private String role;

    // 状态（1正常/0禁用）
    private Byte status;

    // 最后登录时间
    private LocalDateTime lastLoginTime;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
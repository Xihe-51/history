package com.ailab.back.controller;

import com.ailab.back.Entity.User;
import com.ailab.back.service.UserService;
import com.ailab.back.utils.UserContext;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 用户控制器。
 * 权限：登录/注册公开；/user/list、/user/delete/** 仅管理员（由 JwtAuthFilter 统一拦截）；
 * 其余接口需要登录，且普通用户只能操作自己的数据。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 注册 */
    @PostMapping("/register")
    public ResultVO<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    /** 登录 */
    @PostMapping("/login")
    public ResultVO<?> login(@RequestBody User user) {
        return userService.login(user);
    }

    /** 获取当前登录用户信息（前端刷新页面后可用它恢复用户状态） */
    @GetMapping("/me")
    public ResultVO<User> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResultVO.error("未登录");
        }
        return userService.getUserDetail(userId);
    }

    /** 分页查询用户（仅管理员） */
    @GetMapping("/list")
    public ResultVO<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String username
    ) {
        return userService.queryUserBatchByPage(pageNum, pageSize, role, status, username);
    }

    /** 根据ID查询用户（本人或管理员） */
    @GetMapping("/user/{id}")
    public ResultVO<User> getUserDetail(@PathVariable Long id) {
        return userService.getUserDetail(id);
    }

    /** 根据ID修改用户信息（本人可改资料；角色/状态仅管理员） */
    @PutMapping("/update")
    public ResultVO<User> updateUserById(@RequestBody User user) {
        return userService.updateUserById(user);
    }

    /**
     * 修改当前登录用户密码。
     * 请求体：{ "oldPassword": "原密码", "newPassword": "新密码" }
     */
    @PutMapping("/password")
    public ResultVO<?> changePassword(@RequestBody Map<String, String> body) {
        return userService.changePassword(body.get("oldPassword"), body.get("newPassword"));
    }

    /** 删除用户（仅管理员） */
    @DeleteMapping("/delete/{id}")
    public ResultVO<User> deleteUserById(@PathVariable Long id) {
        return userService.deleteUserById(id);
    }
}

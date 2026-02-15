package com.ailab.back.controller;

import com.ailab.back.bean.User;
import com.ailab.back.service.UserService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


// 控制器，处理用户相关的HTTP请求
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    // 注册
    @PostMapping("/register")
    public ResultVO<?> register(@RequestBody User user) {
        return userService.register(user);
    }
    // 登录
    @PostMapping("/login")
    public ResultVO<?> login(@RequestBody User user){
        return userService.login(user);
    }
    // 批量分页查询用户
    @GetMapping("/list")
    public ResultVO<Page<User>> getUserList(
            // 分页参数，设置默认值，避免前端不传参导致空指针
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            // 筛选参数，required = false 表示可选
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String username
    ) { return userService.queryUserBatchByPage(pageNum, pageSize, role, status, username); }
    // 根据id修改用户信息（选择性更新，只更新非null字段）
    @PutMapping("/update")
    public ResultVO<User> updateUserById(@RequestBody User user){ return userService.updateUserById(user); }
    // 根据id删除用户信息
    @DeleteMapping("/delete/{id}")
    public ResultVO<User> deleteUserById(@PathVariable Long id) { return userService.deleteUserById(id); }

}

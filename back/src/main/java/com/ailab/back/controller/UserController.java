package com.ailab.back.controller;

import com.ailab.back.bean.User;
import com.ailab.back.service.UserService;
import com.ailab.back.vo.ResultVO;
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

    @PostMapping("/register")
    public ResultVO<?> register(@RequestBody User user) {
        return userService.register(user);
    }
    @PostMapping("/login")
    public ResultVO<?> login(@RequestBody User user){
        return userService.login(user);
    }
}

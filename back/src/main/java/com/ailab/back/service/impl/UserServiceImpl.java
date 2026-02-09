package com.ailab.back.service.impl;
import com.ailab.back.bean.User;
import com.ailab.back.mapper.UserMapper;
import com.ailab.back.service.UserService;
import com.ailab.back.utils.JwtUtil;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;
    // 注入密码加密器
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    // 注入JWT工具类
    @Autowired
    private JwtUtil jwtUtil;
    /**
     * 注册核心逻辑：
     * 1. 校验参数
     * 2. 用户名/邮箱唯一性校验
     * 3. 设置默认值（角色/状态等）
     * 4. 密码加密
     * 5. 保存用户信息（信息写入数据库）
     * */
    @Override
    public ResultVO<?> register(User user){
        // 1. 基础参数校验
        if (!StringUtils.hasText(user.getUsername())){
            return ResultVO.error("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPasswordHash())){
            return ResultVO.error("密码不能为空");
        }
        if (!StringUtils.hasText(user.getEmail())){
            return ResultVO.error("邮箱不能为空");
        }
        // 2. 用户名唯一性
        User existUserByUsername = userMapper.selectUserByUsername(user.getUsername());
        if(existUserByUsername != null){
           return ResultVO.error("用户名已存在");
        }
        // 3. 设置默认值
        user.setRole("user"); // 默认普通用户
        user.setStatus((byte) 1); // 默认账号正常
        user.setAvatarPath("/images/default.png"); //默认头像
        // 4. 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(encodedPassword);
        // 5. 保存用户信息
        boolean saveSuccess = this.save(user);
        if(saveSuccess){
            user.setPasswordHash(null); //清空密码，避免返回给前端
            return ResultVO.success("注册成功").setData(user);
        } else {
            return ResultVO.error("注册失败，请重试");
        }
    }
    /**
     * 登录核心逻辑
     * 1. 参数校验
     * 2. 根据用户名查询用户
     * 3. 校验用户是否存在
     * 4. 校验用户状态
     * 5. 密码验证
     * 6. 生成JWT token令牌并返回响应
     * 7. 更新用户最后登录时间
     * */
    @Override
    public ResultVO<?> login(User user) {
        // 1. 基础参数校验
        if(!StringUtils.hasText(user.getUsername())) {
            return ResultVO.error("用户名不能为空");
        }
        if(!StringUtils.hasText(user.getPasswordHash())) {
            return ResultVO.error("密码不能为空");
        }

        // 2. 根据用户名查询用户
        User loginUser = userMapper.selectUserByUsername(user.getUsername());

        // 3. 校验用户是否存在
        if(loginUser==null){
            return ResultVO.error("用户名不存在");
        }

        // 4. 校验用户状态 （0=禁用 1=正常）
        if (loginUser.getStatus()==0){
            return ResultVO.error("账号已被禁用，请联系管理员");
        }

        // 5. 密码验证
        boolean passwordMatch = passwordEncoder.matches(user.getPasswordHash(), loginUser.getPasswordHash());
        if(!passwordMatch) {
            return ResultVO.error("密码错误");
        }

        // ========== 更新最后登录时间 ==========
        try {
            // 1. 获取当前时间，格式化为数据库兼容的字符串（如：2026-02-10 12:30:45）
            LocalDateTime now = LocalDateTime.now();
            // 格式化器：适配MySQL的datetime格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String lastLoginTime = now.format(formatter);

            // 2. 调用Mapper方法更新最后登录时间
            int affectedRows = userMapper.updateLastLoginTime(loginUser.getId(), lastLoginTime);
        } catch (Exception e) {
            // 捕获异常，避免更新登录时间失败导致登录流程中断
            System.err.println("更新最后登录时间异常：" + e.getMessage());
        }

        // 6.JWT Token
        String token = jwtUtil.generateToken(loginUser.getId(),loginUser.getUsername());

        // 构造返回数据
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("token", token);
        resultData.put("tokenExpire", 2*60*60);
        resultData.put("userId",loginUser.getId());
        resultData.put("username", loginUser.getUsername());
        resultData.put("role", loginUser.getRole());

        return ResultVO.success("登录成功").setData(resultData);
    }
}

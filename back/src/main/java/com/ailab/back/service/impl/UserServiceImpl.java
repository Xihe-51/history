package com.ailab.back.service.impl;

import com.ailab.back.Entity.User;
import com.ailab.back.mapper.UserMapper;
import com.ailab.back.service.UserService;
import com.ailab.back.utils.JwtUtil;
import com.ailab.back.utils.UserContext;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /** 邮箱格式校验 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 注册：
     * 1. 校验参数 2. 唯一性校验 3. 设置默认值 4. 密码加密 5. 保存
     */
    @Override
    public ResultVO<?> register(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            return ResultVO.error("用户名不能为空");
        }
        String username = user.getUsername().trim();
        if (username.length() < 3 || username.length() > 20) {
            return ResultVO.error("用户名长度需在 3 到 20 个字符之间");
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            return ResultVO.error("密码不能为空");
        }
        if (user.getPasswordHash().length() < MIN_PASSWORD_LENGTH) {
            return ResultVO.error("密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            return ResultVO.error("邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            return ResultVO.error("邮箱格式不正确");
        }

        if (userMapper.selectUserByUsername(username) != null) {
            return ResultVO.error("用户名已存在");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(user.getEmail().trim());
        newUser.setRole("user");
        newUser.setStatus((byte) 1);
        newUser.setAvatarPath("/images/default.png");
        newUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        boolean saveSuccess = this.save(newUser);
        if (!saveSuccess) {
            return ResultVO.error("注册失败，请重试");
        }
        newUser.setPasswordHash(null);
        return ResultVO.success("注册成功").setData(newUser);
    }

    /**
     * 登录：校验密码、账号状态，返回 JWT（包含角色）。
     */
    @Override
    public ResultVO<?> login(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            return ResultVO.error("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            return ResultVO.error("密码不能为空");
        }

        User loginUser = userMapper.selectUserByUsername(user.getUsername().trim());
        // 统一提示，避免被用于枚举用户名
        if (loginUser == null) {
            return ResultVO.error("用户名或密码错误");
        }
        if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
            return ResultVO.error("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(user.getPasswordHash(), loginUser.getPasswordHash())) {
            return ResultVO.error("用户名或密码错误");
        }

        try {
            userMapper.updateLastLoginTime(loginUser.getId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            // 更新失败不影响登录
            log.warn("更新最后登录时间失败: {}", e.getMessage());
        }

        String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getUsername(), loginUser.getRole());

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("token", token);
        resultData.put("tokenExpire", 2 * 60 * 60);
        resultData.put("userId", loginUser.getId());
        resultData.put("username", loginUser.getUsername());
        resultData.put("role", loginUser.getRole());
        resultData.put("avatarPath", loginUser.getAvatarPath());
        resultData.put("email", loginUser.getEmail());

        return ResultVO.success("登录成功").setData(resultData);
    }

    @Override
    public ResultVO<Page<User>> queryUserBatchByPage(Integer pageNum, Integer pageSize,
                                                     String role, Byte status, String username) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE ? 10 : pageSize;

        Page<User> userPage = userMapper.selectUserBatchByPage(new Page<>(pageNum, pageSize), role, status, username);
        // 绝不把密码哈希返回给前端
        if (userPage.getRecords() != null) {
            userPage.getRecords().forEach(u -> u.setPasswordHash(null));
        }
        return ResultVO.<Page<User>>success().setData(userPage);
    }

    @Override
    public ResultVO<User> getUserDetail(Long id) {
        UserContext.CurrentUser current = UserContext.get();
        // 普通用户只能查看自己
        if (current == null || (!current.isAdmin() && !current.userId().equals(id))) {
            return ResultVO.error("无权查看该用户信息");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return ResultVO.error("未找到用户");
        }
        user.setPasswordHash(null);
        return ResultVO.<User>success().setData(user);
    }

    /**
     * 更新用户信息。
     *
     * <p>权限规则（修复越权）：普通用户只能修改自己的邮箱/用户名/头像/密码；
     * 角色(role)与状态(status)只能由管理员修改，避免普通用户把自己改成 admin。
     */
    @Override
    public ResultVO<User> updateUserById(User user) {
        UserContext.CurrentUser current = UserContext.get();
        if (current == null) {
            return ResultVO.error("未登录");
        }
        if (user.getId() == null) {
            return ResultVO.error("用户ID不能为空");
        }

        boolean isSelf = current.userId().equals(user.getId());
        boolean isAdmin = current.isAdmin();
        if (!isSelf && !isAdmin) {
            return ResultVO.error("无权修改其他用户信息");
        }
        if (userMapper.selectById(user.getId()) == null) {
            return ResultVO.error("用户不存在");
        }

        // 普通用户（含修改自己）不允许改角色和状态
        if (!isAdmin) {
            if (StringUtils.hasText(user.getRole()) || user.getStatus() != null) {
                user.setRole(null);
                user.setStatus(null);
            }
        } else if (isSelf && StringUtils.hasText(user.getRole())
                && !current.role().equalsIgnoreCase(user.getRole())) {
            // 管理员也不能降低/改变自己的角色，避免把自己锁在门外
            return ResultVO.error("不能修改自己的角色");
        }

        // 用户名唯一性
        if (StringUtils.hasText(user.getUsername())) {
            User sameName = userMapper.selectUserByUsername(user.getUsername().trim());
            if (sameName != null && !sameName.getId().equals(user.getId())) {
                return ResultVO.error("用户名已存在");
            }
        }

        // 邮箱格式
        if (StringUtils.hasText(user.getEmail()) && !EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            return ResultVO.error("邮箱格式不正确");
        }

        // 密码：为空不修改；否则加密并校验长度
        if (StringUtils.hasText(user.getPasswordHash())) {
            if (user.getPasswordHash().length() < MIN_PASSWORD_LENGTH) {
                return ResultVO.error("密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
            }
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        } else {
            user.setPasswordHash(null);
        }

        // 说明：本接口只更新用户资料，passwordHash 字段为“直接设置/重置”，不校验旧密码。
        // 管理员用它重置他人密码；需要校验旧密码的本人改密请走 /user/password 接口。
        int success = userMapper.updateUserById(user);
        if (success > 0) {
            return ResultVO.success("更新成功");
        }
        return ResultVO.error("更新失败，没有需要修改的内容");
    }

    /**
     * 删除用户：仅管理员（JwtAuthFilter 已拦截），且不能删除自己 / 最后一个管理员。
     */
    @Override
    public ResultVO<User> deleteUserById(Long id) {
        UserContext.CurrentUser current = UserContext.get();
        if (current != null && current.userId().equals(id)) {
            return ResultVO.error("不能删除当前登录账号");
        }
        User target = userMapper.selectById(id);
        if (target == null) {
            return ResultVO.error("用户不存在");
        }
        if ("admin".equalsIgnoreCase(target.getRole())) {
            Long adminCount = this.count(new LambdaQueryWrapper<User>().eq(User::getRole, "admin"));
            if (adminCount != null && adminCount <= 1) {
                return ResultVO.error("系统至少需要保留一个管理员账号");
            }
        }
        int success = userMapper.deleteUserById(id);
        if (success > 0) {
            return ResultVO.success("删除成功");
        }
        return ResultVO.error("删除失败");
    }

    /**
     * 校验旧密码（用于“修改密码”场景）。
     */
    @Override
    public ResultVO<?> changePassword(String oldPassword, String newPassword) {
        UserContext.CurrentUser current = UserContext.get();
        if (current == null) {
            return ResultVO.error("未登录");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return ResultVO.error("新密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }
        if (!StringUtils.hasText(oldPassword)) {
            return ResultVO.error("请输入原密码");
        }
        User dbUser = userMapper.selectById(current.userId());
        if (dbUser == null) {
            return ResultVO.error("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, dbUser.getPasswordHash())) {
            return ResultVO.error("原密码不正确");
        }
        User update = new User();
        update.setId(current.userId());
        update.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateUserById(update);
    }

    /** 供 Controller 统一整理返回字段，避免各处遗漏 */
    public static List<User> stripPasswords(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        users.forEach(u -> u.setPasswordHash(null));
        return users;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);
}

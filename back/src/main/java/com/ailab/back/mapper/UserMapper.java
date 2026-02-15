package com.ailab.back.mapper;

import com.ailab.back.bean.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * 用户表映射接口
 * 继承BaseMapper获得MyBatis-Plus基础CRUD能力，自定义方法在XML中实现
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 1. 根据用户名查询用户（精确匹配）
     * @param username 用户名
     * @return 用户实体（null表示未找到）
     */
    User selectUserByUsername(@Param("username") String username);

    /**
     * 2. 根据ID修改用户信息（选择性更新，只更新非null字段）
     * @param user 用户实体（必须包含id，其他字段非null则更新）
     * @return 受影响的行数（1表示成功，0表示未找到该用户）
     */
    int updateUserById(@Param("user") User user);

    /**
     * 3. 批量查询用户数据（支持条件筛选，非分页）
     * @param role 角色筛选条件（可选，null则不筛选）
     * @param status 状态筛选条件（可选，null则不筛选）
     * @return 用户列表
     */
    List<User> selectUserBatch(@Param("role") String role, @Param("status") Byte status);

    /**
     * 3. 批量查询用户数据（分页版）
     * @param page 分页对象（MyBatis-Plus分页插件自动处理）
     * @param role 角色筛选条件（可选）
     * @param status 状态筛选条件（可选）
     * @param username 用户名模糊查询条件（可选）
     * @return 分页后的用户列表
     */
    Page<User> selectUserBatchByPage(
            Page<User> page,
            @Param("role") String role,
            @Param("status") Byte status,
            @Param("username") String username
    );

    /**
     * 4. 根据ID删除用户
     * @param id 用户ID
     * @return 受影响的行数（1表示成功，0表示未找到该用户）
     */
    int deleteUserById(@Param("id") Long id);

    /**
     * 更新用户最后登录时间
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 受影响的行数
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") String lastLoginTime);
}

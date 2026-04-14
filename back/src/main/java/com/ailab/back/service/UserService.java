package com.ailab.back.service;
import com.ailab.back.Entity.User;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;


// 用户服务类接口
// 继承IService获得Mybatis-plus提供的基础服务方法
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param user 注册的用户信息
     * @return 注册结果
     * */
    ResultVO<?> register(User user);
    /**
     * 用户登录
     * @param user 登录的用户信息
     * @return 登陆结果
     * */
    ResultVO<?> login(User user);

    /**
     * 分页批量查询用户数据（支持用户名模糊、角色、状态筛选）
     * @param pageNum 当前页码（前端传入，从1开始）
     * @param pageSize 每页条数
     * @param role 角色筛选条件（可选，null/空则不筛选）
     * @param status 状态筛选条件（可选，null则不筛选）
     * @param username 用户名模糊查询关键词（可选，null/空则不筛选）
     * @return 分页后的用户数据（包含列表和分页信息）
     */
    ResultVO<Page<User>> queryUserBatchByPage(Integer pageNum,
                                              Integer pageSize,
                                              String role,
                                              Byte status,
                                              String username);
    /**
     * 根据查询用户信息
     * */
    ResultVO<User> getUserDetail(Long id);
    /**
     * 根据id修改用户信息
     * */
    ResultVO<User> updateUserById(User user);
    /**
     * 根据id删除用户
     * */
    ResultVO<User> deleteUserById(Long id);
}

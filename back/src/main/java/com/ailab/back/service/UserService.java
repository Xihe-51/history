package com.ailab.back.service;
import com.ailab.back.bean.User;
import com.ailab.back.vo.ResultVO;
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
}

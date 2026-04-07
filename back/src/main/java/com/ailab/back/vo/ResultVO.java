package com.ailab.back.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结果封装类
 * 使用泛型T支持不同类型的数据返回
 */
@Data // Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@AllArgsConstructor // Lombok注解：生成全参数构造函数
@NoArgsConstructor // Lombok注解：生成无参数构造函数
public class ResultVO<T> {

    /**
     * 响应状态码
     * 200: 成功
     * 500: 系统错误
     * 其他: 可根据业务需求扩展
     */
    private Integer code;

    /**
     * 响应消息
     * 用于描述操作结果或错误信息
     */
    private String msg;

    /**
     * 响应数据
     * 泛型T支持任意类型的业务数据
     */
    private T data;

    /**
     * 设置响应数据（链式调用支持）
     *
     * @param data 要设置的业务数据
     * @return 当前ResultVO对象（支持链式调用）
     */
    public ResultVO<T> setData(T data) {
        this.data = data;
        return this;
    }

    /* 预定义常量 */

    /**
     * 成功状态码常量
     */
    private final static Integer SUCCESS_CODE = 200;

    /**
     * 错误状态码常量
     */
    private final static Integer ERROR_CODE = 500;

    /**
     * 默认错误消息
     */
    private final static String ERRROR_MSG = "操作失败";

    /**
     * 默认成功消息
     */
    private final static String SUCCESS_MSG = "操作成功";


    /**
     * 创建成功响应（使用默认消息，无数据）
     * @param <T> 泛型类型
     * @return 成功响应的ResultVO对象
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<T>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    /**
     * 创建错误响应（使用默认消息，无数据）
     * @param <T> 泛型类型
     * @return 错误响应的ResultVO对象
     */
    public static <T> ResultVO<T> error() {
        return new ResultVO<T>(ERROR_CODE, ERRROR_MSG, null);
    }

    /**
     * 创建成功响应（自定义消息，无数据）
     * @param msg 自定义成功消息
     * @param <T> 泛型类型
     * @return 成功响应的ResultVO对象
     */
    public static <T> ResultVO<T> success(String msg) {
        return new ResultVO<T>(SUCCESS_CODE, msg, null);
    }

    /**
     * 创建错误响应（自定义消息，无数据）
     * @param msg 自定义错误消息
     * @param <T> 泛型类型
     * @return 错误响应的ResultVO对象
     */
    public static <T> ResultVO<T> error(String msg) {
        return new ResultVO<T>(ERROR_CODE, msg, null);
    }


    /**
     * 创建成功响应（使用默认消息，带数据）
     * @param data 业务数据
     * @param <T> 泛型类型
     * @return 成功响应的ResultVO对象
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<T>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    /**
     * 创建成功响应（自定义消息，带数据）
     * @param msg 自定义成功消息
     * @param data 业务数据
     * @param <T> 泛型类型
     * @return 成功响应的ResultVO对象
     */
    public static <T> ResultVO<T> success(String msg, T data) {
        return new ResultVO<T>(SUCCESS_CODE, msg, data);
    }

    /**
     * 创建错误响应（自定义消息，带数据）
     * @param msg 自定义错误消息
     * @param data 业务数据
     * @param <T> 泛型类型
     * @return 错误响应的ResultVO对象
     */
    public static <T> ResultVO<T> error(String msg, T data) {
        return new ResultVO<T>(ERROR_CODE, msg, data);
    }
}
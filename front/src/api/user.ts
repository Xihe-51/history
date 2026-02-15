import request from "@/utils/request";

/**
 * 用户注册接口
 * @param user - 用户注册信息对象
 * @returns 注册结果
 */
export const register = (user: any) => {
    return request.post("/user/register", user);
};

/**
 * 用户登录接口
 * @param user - 用户登录信息（包含用户名/密码等）
 * @returns 登录结果（通常包含token、用户信息等）
 */
export const login = (user: any) => {
    return request.post("/user/login", user);
};

/**
 * 批量分页查询用户列表
 * @param params - 查询参数
 * @param params.pageNum - 页码（默认1）
 * @param params.pageSize - 每页条数（默认10）
 * @param params.role - 角色（可选）
 * @param params.status - 状态（可选）
 * @param params.username - 用户名（可选）
 * @returns 分页用户列表
 */
export const getUserList = (params: {
    pageNum?: number;
    pageSize?: number;
    role?: string;
    status?: number;
    username?: string;
}) => {
    // 设置默认参数
    const queryParams = {
        pageNum: 1,
        pageSize: 10,
        ...params,
    };
    return request.get("/user/list", { params: queryParams });
};

/**
 * 根据ID修改用户信息（选择性更新）
 * @param user - 用户信息对象（必须包含id，其他字段非必填）
 * @returns 更新后的用户信息
 */
export const updateUserById = (user: any) => {
    return request.put("/user/update", user);
};

/**
 * 根据ID删除用户
 * @param id - 用户ID
 * @returns 删除结果
 */
export const deleteUserById = (id: number | string) => {
    return request.delete(`/user/delete/${id}`);
};
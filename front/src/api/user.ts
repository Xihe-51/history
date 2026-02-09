import request  from "@/utils/request";

export const register = (user: any) => {
    return request.post("/user/register", user)
}

export const login = (user: any) => {
    return request.post("/user/login", user)
}
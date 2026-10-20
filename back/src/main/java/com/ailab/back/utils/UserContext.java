package com.ailab.back.utils;

/**
 * 当前登录用户上下文。
 * 由 JwtAuthFilter 在请求进入时写入，在请求结束时清理，
 * Service 层通过本类获取“当前登录用户”而不是信任前端传入的 userId。
 */
public final class UserContext {

    /** 登录用户信息快照 */
    public record CurrentUser(Long userId, String username, String role) {
        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(role);
        }
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 当前用户ID，未登录返回 null */
    public static Long getUserId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    /** 当前用户角色，未登录返回 null */
    public static String getRole() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.role();
    }

    public static boolean isAdmin() {
        CurrentUser u = HOLDER.get();
        return u != null && u.isAdmin();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

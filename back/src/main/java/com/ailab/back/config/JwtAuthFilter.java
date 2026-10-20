package com.ailab.back.config;

import com.ailab.back.Entity.User;
import com.ailab.back.mapper.UserMapper;
import com.ailab.back.utils.JwtUtil;
import com.ailab.back.utils.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器。
 *
 * <p>职责：
 * <ol>
 *   <li>解析 Authorization: Bearer &lt;token&gt;，校验签名与有效期；</li>
 *   <li>回查数据库，确保账号仍然存在且未被禁用（防止拿着旧 token 继续操作被禁用的账号）；</li>
 *   <li>把当前用户写入 {@link UserContext}，供后续 Controller/Service 使用；</li>
 *   <li>对写操作（新增/修改/删除/导入/备份/上传）以及用户管理接口做角色校验。</li>
 * </ol>
 *
 * <p>注意：过滤器运行在 Spring Security 过滤链之前，因此由本类负责 401/403 响应，
 * 响应体与全局接口规范保持一致（code/msg/data）。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /** 无需登录即可访问的 GET 接口（科普内容浏览 / 趣味互动答题取题） */
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/dynasty", "/dynasty-stage", "/historical-event", "/historical-person", "/allusion", "/question"
    );

    /**
     * 静态资源前缀：图片/视频等上传文件必须可匿名访问，
     * 否则 <img src> 无法携带 Authorization 头，页面上的图片会全部 401。
     */
    private static final List<String> PUBLIC_STATIC_PREFIXES = List.of(
            "/images/", "/uploads/", "/static/", "/maps/"
    );

    /** 根路径下的静态图片后缀（历史数据里的事件配图写作 /dayuzhishui.jpg） */
    private static final List<String> STATIC_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".ico", ".svg", ".bmp",
            ".mp4", ".webm", ".mov", ".mp3", ".wav", ".css", ".js", ".woff", ".woff2", ".ttf"
    );

    /** 判断是否为静态资源请求（按路径后缀） */
    private static boolean isStaticAsset(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        for (String ext : STATIC_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /** 完全公开的接口（登录 / 注册 / 静态资源由 Security 处理） */
    private static final List<String> FULLY_PUBLIC = List.of(
            "/user/login", "/user/register"
    );

    /** 仅管理员可访问的路径前缀（任何方法） */
    private static final List<String> ADMIN_ONLY_PREFIXES = List.of(
            "/user/list", "/user/delete", "/data", "/content"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            // 去掉 context-path(/api)，便于与业务路径比较
            String ctx = request.getContextPath();
            if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
                path = path.substring(ctx.length());
            }
            String method = request.getMethod();

            // OPTIONS 预检请求直接放行
            if (HttpMethod.OPTIONS.matches(method)) {
                chain.doFilter(request, response);
                return;
            }

            // 1. 尝试解析 Token（无论是公开接口还是受保护接口，有 token 就识别身份）
            String token = extractToken(request);
            if (token != null) {
                UserContext.CurrentUser currentUser = resolveUser(token);
                if (currentUser != null) {
                    UserContext.set(currentUser);
                }
            }

            // 2. 完全公开接口（登录、注册）直接放行
            if (FULLY_PUBLIC.contains(path)) {
                chain.doFilter(request, response);
                return;
            }

            boolean isPublicGet = HttpMethod.GET.matches(method) && startsWithAny(path, PUBLIC_GET_PREFIXES);
            if (isPublicGet) {
                chain.doFilter(request, response);
                return;
            }

            // 静态资源（图片/视频）匿名可读
            if (startsWithAny(path, PUBLIC_STATIC_PREFIXES) || isStaticAsset(path)) {
                chain.doFilter(request, response);
                return;
            }

            // 3. 只读用户信息接口：需登录
            //   /user/user/{id} 允许本人或管理员查看（由 Controller 再做资源级校验）
            // 4. 其余接口一律要求已登录
            if (UserContext.get() == null) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "未登录或登录已过期，请重新登录");
                return;
            }

            // 5. 管理员专属接口
            if (startsWithAny(path, ADMIN_ONLY_PREFIXES) && !UserContext.isAdmin()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, 403, "权限不足，仅管理员可操作");
                return;
            }

            // 6. 内容写操作（新增/修改/删除）仅管理员
            boolean isContentWrite = !HttpMethod.GET.matches(method) && startsWithAny(path, PUBLIC_GET_PREFIXES);
            if (isContentWrite && !UserContext.isAdmin()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, 403, "权限不足，仅管理员可修改科普数据");
                return;
            }

            // 7. 文件上传：图片允许普通用户（头像），视频/其他仅管理员
            if ("/file/upload".equals(path) && !UserContext.isAdmin()) {
                String resourceType = request.getParameter("resourceType");
                if (resourceType != null && !"图片".equals(resourceType)) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, 403, "普通用户仅可上传图片");
                    return;
                }
            }

            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private static boolean startsWithAny(String path, List<String> prefixes) {
        for (String p : prefixes) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String t = header.substring(7).trim();
            return t.isEmpty() ? null : t;
        }
        return null;
    }

    /**
     * 解析 Token 并回查数据库。
     * 返回 null 表示 token 无效/过期/用户被禁用，此时按“未登录”处理。
     */
    private UserContext.CurrentUser resolveUser(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            Object uid = claims.get(JwtUtil.CLAIM_USER_ID);
            if (uid == null) {
                return null;
            }
            Long userId = Long.valueOf(String.valueOf(uid));
            User user = userMapper.selectById(userId);
            if (user == null) {
                return null;
            }
            // 被禁用账号的 token 立即失效
            if (user.getStatus() != null && user.getStatus() == 0) {
                return null;
            }
            // 角色以数据库为准（管理员降权后旧 token 不再具有管理员权限）
            return new UserContext.CurrentUser(user.getId(), user.getUsername(), user.getRole());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private void writeError(HttpServletResponse response, int httpStatus, int code, String msg) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("msg", msg);
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

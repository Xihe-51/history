package com.ailab.back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Web MVC / Spring Security 配置类
 * 处理跨域、静态资源映射、密码加密与 JWT 过滤器注册。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthFilter;

    /** 上传文件物理目录（纯文件系统路径，不含 file: 前缀） */
    @Value("${image.upload.path}")
    private String imageUploadPath;

    /** 前端访问前缀，默认 /images/ */
    @Value("${image.access.prefix:/images/}")
    private String imageAccessPrefix;

    /**
     * 跨域配置：使用 CorsConfigurationSource 交给 Spring Security 处理，
     * 确保 401/403 的响应也带有 CORS 头（否则浏览器只会看到 “网络错误” 而拿不到具体原因）。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许任意来源（本地开发常见：localhost / 127.0.0.1 / 局域网 IP 访问 5173）
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 静态资源映射。
     *
     * <p>说明：历史数据里的图片路径存在多种写法，必须全部覆盖，否则页面图片会 404：
     * <ul>
     *   <li>事件配图：{@code /dayuzhishui.jpg}（根路径）</li>
     *   <li>朝代地图：{@code /maps/qin.jpg}</li>
     *   <li>默认头像：{@code /images/default.png}</li>
     *   <li>新上传文件：{@code /images/<uuid>.png}</li>
     * </ul>
     * 解决方式：上传文件以“真实文件优先，classpath 内置资源兜底”的顺序组成多位置查找链。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = imageAccessPrefix.endsWith("/") ? imageAccessPrefix : imageAccessPrefix + "/";

        // 1) 上传目录优先；找不到时回落到 jar 内置图片（例如默认头像 default.png）
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations(toDirUri(imageUploadPath), "classpath:/static/images/")
                .setCachePeriod(0);

        // 2) 根路径下的内置图片：/dayuzhishui.jpg 这类历史数据
        registry.addResourceHandler("/*.jpg", "/*.jpeg", "/*.png", "/*.gif", "/*.webp", "/*.ico")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(0);

        // 3) 朝代地图：/maps/qin.jpg
        registry.addResourceHandler("/maps/**")
                .addResourceLocations("classpath:/static/maps/")
                .setCachePeriod(0);
    }

    private static String toDirUri(String rawPath) {
        String p = rawPath == null ? "" : rawPath.trim();
        if (p.startsWith("file:")) {
            p = p.substring("file:".length());
        }
        Path path = Paths.get(p).toAbsolutePath().normalize();
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    /** BCrypt 密码加密器 */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security 过滤链：
     * - 关闭 CSRF（前后端分离、无 Session）
     * - 无状态会话
     * - 启用 CORS
     * - 在 UsernamePasswordAuthenticationFilter 之前执行 JWT 过滤器完成鉴权
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 鉴权集中在 JwtAuthFilter 中完成，这里放行并交由过滤器判权
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

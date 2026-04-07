package com.ailab.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 处理跨域、静态资源映射、Spring Security配置（密码加密+权限放行）
 */
@Configuration
@EnableWebSecurity // 开启Spring Security配置
public class WebMvcConfig implements WebMvcConfigurer {
    // 从yml配置文件中读取图片存储路径
    @Value("${image.upload.path}")
    private String imageUploadPath;

    // 从yml配置文件中读取访问前缀
    @Value("${image.access.prefix}")
    private String imageAccessPrefix;
    /**
     * 配置跨域请求
     * 解决前后端分离时的跨域问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有接口跨域
                .allowedOriginPatterns("*") // 允许所有域名（生产环境建议指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带Cookie
                .maxAge(3600); // 预检请求有效期（秒）
    }

    /**
     * 配置静态资源映射：将URL路径 /images/** 映射到物理目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用配置文件中的路径和前缀
        registry.addResourceHandler(imageAccessPrefix + "**") // URL路径匹配
                .addResourceLocations(imageUploadPath) // 物理目录
                .setCachePeriod(0); // 关闭缓存，方便开发调试
    }

    // ======================== Spring Security相关配置（适配6.x版本） ========================
    /**
     * 配置BCrypt密码加密器（用于密码加密）
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置Security过滤链（使用6.x非弃用API）
     * 放行所有请求，关闭CSRF
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. 关闭CSRF
        http.csrf(AbstractHttpConfigurer::disable);

        // 2. 关闭HTTP Basic认证
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 3. 设置会话策略为无状态（前后端分离推荐）
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. 授权规则：所有请求允许匿名访问
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
        );

        return http.build();
    }
}
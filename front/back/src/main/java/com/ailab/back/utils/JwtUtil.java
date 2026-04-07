package com.ailab.back.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 生成Token、解析Token、验证Token
 */
@Component
public class JwtUtil {

    // 秘钥（建议生产环境放在配置文件中，至少32位）
    private static final String SECRET_KEY = "ailab-back-secret-key-1234567890-abcdefghijklmn";

    // Token过期时间：2小时（单位：毫秒）
    private static final long EXPIRATION_TIME = 2 * 60 * 60 * 1000L;

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的Token字符串
     */
    public String generateToken(Long userId, String username) {
        // 1. 生成加密秘钥
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        // 2. 设置Token的载荷（自定义Claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 3. 生成Token
        return Jwts.builder()
                .setClaims(claims) // 设置载荷
                .setSubject(username) // 设置主题（用户名）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .signWith(key) // 签名
                .compact();
    }

    /**
     * 解析Token，获取载荷信息
     * @param token Token字符串
     * @return 载荷Claims
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     * @param token Token字符串
     * @return true=未过期，false=已过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
}
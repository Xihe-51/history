package com.ailab.back.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 生成Token、解析Token、验证Token
 */
@Component
public class JwtUtil {

    /**
     * 签名秘钥：通过 jwt.secret 配置，缺省时使用内置值（至少32字节）。
     * 生产环境请务必通过环境变量 JWT_SECRET 覆盖。
     */
    @Value("${jwt.secret:ailab-back-secret-key-1234567890-abcdefghijklmn}")
    private String secretKey;

    /** Token过期时间：默认2小时（单位：毫秒），可通过 jwt.expiration 配置 */
    @Value("${jwt.expiration:7200000}")
    private long expirationTime;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（admin/user），写入Token便于网关/过滤器做权限判断
     * @return 生成的Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role == null ? "user" : role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key())
                .compact();
    }

    /**
     * 解析Token，获取载荷信息。Token非法/过期时抛出 JwtException。
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
}

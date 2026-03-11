package com.coldchain.guardian.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForColdChainGuardianChangeThisToARealSecretThatIsAtLeast256BitsLong}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration; // 默认24小时过期

    private SecretKey getSigningKey() {
        // 使用HS256算法，需要至少256位（32字节）的密钥
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从HTTP请求中提取JWT Token
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        final String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("created", new Date());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)                  // 关键：subject 放用户名
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 改为HS256
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims == null ? null : claims.getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims != null ? claims.get("userId", Long.class) : null;
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    /**
     * 从Token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        String role;
        try {
            Claims claims = getClaimsFromToken(token);
            role = claims != null ? (String) claims.get("role") : null;
        } catch (Exception e) {
            role = null;
        }
        return role;
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            Claims claims = getClaimsFromToken(token);
            expiration = claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    /**
     * 验证Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 验证Token有效性
     */
    public Boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return false;
        Date exp = claims.getExpiration();
        return exp != null && exp.after(new Date());
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 生成过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }
}
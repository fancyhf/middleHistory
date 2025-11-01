/**
 * JWT工具类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:25:00
 * @description JWT令牌的生成、验证和解析工具
 */
package com.historyanalysis.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 
 * 功能：
 * - 生成JWT令牌
 * - 验证JWT令牌
 * - 从JWT中提取信息
 * - 检查令牌是否过期
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // JWT密钥
    @Value("${jwt.secret:historyAnalysisSecretKeyForJwtTokenGeneration2024}")
    private String jwtSecret;

    // JWT过期时间（毫秒）
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 从JWT令牌中提取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取指定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从JWT令牌中提取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查JWT令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为用户生成JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 为用户生成JWT令牌（带额外声明）
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 创建JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证JWT令牌
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 验证JWT令牌（不需要UserDetails）
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            logger.error("JWT签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT令牌格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT声明字符串为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 刷新JWT令牌
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration));
            
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("刷新JWT令牌失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取JWT令牌的剩余有效时间（毫秒）
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("获取JWT令牌剩余时间失败: {}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 从JWT令牌中提取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("userId", String.class);
        } catch (Exception e) {
            logger.error("从JWT令牌提取用户ID失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从JWT令牌中提取用户角色
     */
    public String getUserRoleFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("从JWT令牌提取用户角色失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
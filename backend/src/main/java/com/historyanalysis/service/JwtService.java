/**
 * JWT服务类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT服务类
 * 提供JWT令牌的生成、解析、验证等功能
 */
@Service
public class JwtService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 从JWT令牌中提取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取指定声明
     * 
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T> 声明类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 生成JWT令牌
     * 
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 生成带有额外声明的JWT令牌
     * 
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * 生成刷新令牌
     * 
     * @param userDetails 用户详情
     * @return 刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * 验证JWT令牌是否有效
     * 
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查JWT令牌是否过期
     * 
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 构建JWT令牌
     * 
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @param expiration 过期时间（毫秒）
     * @return JWT令牌
     */
    private String buildToken(Map<String, Object> extraClaims, 
                             UserDetails userDetails, 
                             long expiration) {
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中提取所有声明
     * 
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取令牌剩余有效时间（毫秒）
     * 
     * @param token JWT令牌
     * @return 剩余有效时间
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return Math.max(0, expiration.getTime() - now.getTime());
    }

    /**
     * 从令牌中提取用户ID（如果存在）
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 从令牌中提取用户角色（如果存在）
     * 
     * @param token JWT令牌
     * @return 用户角色
     */
    public String extractUserRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }
}
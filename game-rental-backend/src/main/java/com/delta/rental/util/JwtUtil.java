package com.delta.rental.util;

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
 * JWT 签发与校验工具。签名密钥与有效期来自配置项 {@code jwt.secret} / {@code jwt.expiration}。
 *
 * <p>{@link #parsePayload(String)} 会<b>校验签名</b>，令牌被篡改时抛
 * {@link io.jsonwebtoken.JwtException}，调用方需捕获。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 已验签的令牌载荷。
     *
     * @param userId   用户 id
     * @param username 用户名
     * @param role     角色（0=普通用户，1=卖家，2=管理员）
     * @param expiration 过期时间
     */
    public record JwtPayload(Long userId, String username, Integer role, Date expiration) {
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析并校验签名，返回原始 Claims。
     *
     * @throws io.jsonwebtoken.JwtException 令牌非法、签名不匹配或已过期
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 一次性解析出业务需要的载荷字段（避免重复解析同一令牌）。
     *
     * @throws io.jsonwebtoken.JwtException 令牌非法、签名不匹配
     */
    public JwtPayload parsePayload(String token) {
        Claims claims = parseToken(token);
        return new JwtPayload(
                Long.valueOf(claims.get("userId").toString()),
                claims.getSubject(),
                Integer.valueOf(claims.get("role").toString()),
                claims.getExpiration()
        );
    }

    public Long getUserId(String token) {
        return parsePayload(token).userId();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Integer getRole(String token) {
        return parsePayload(token).role();
    }

    /** 校验签名并判断是否已过期；令牌非法时返回 {@code true}。 */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /** 已验签载荷是否已过期。 */
    public boolean isExpired(JwtPayload payload) {
        return payload.expiration() == null || payload.expiration().before(new Date());
    }
}

package com.knowledge.knowledgeagentplatform.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * jwt工具类
 *  * JWT 工具类（jjwt 0.13.x API）
 *  *
 *  * 双 Token 设计：
 *  *  - accessToken：短期（默认 2 小时），每次请求携带，用于接口鉴权
 *  *  - refreshToken：长期（默认 7 天），只用于换取新 accessToken，不用于普通接口
 *  *
 *  * 安全要点：
 *  *  - HS256 要求密钥 >= 256 bit（32 字节），启动时校验
 *  *  - 密钥从配置读取，放环境变量，绝不硬编码
 */
@Component
public class JwtUtil {
    /** token 类型标识，放在 payload 里，防止拿 refreshToken 直接调业务接口 */
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpireMillis;
    private final long refreshExpireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expire-minutes:120}") long accessExpireMinutes,
                   @Value("${jwt.refresh-expire-days:7}") long refreshExpireDays) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret 长度必须 >= 32 字节（HS256 安全要求）");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireMillis = accessExpireMinutes * 60 * 1000;
        this.refreshExpireMillis = refreshExpireDays * 24 * 60 * 60 * 1000;
    }

    /** 生成 accessToken */
    public String createAccessToken(Long userId, String username) {
        return createToken(userId, username, TYPE_ACCESS, accessExpireMillis);
    }

    /** 生成 refreshToken */
    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, TYPE_REFRESH, refreshExpireMillis);
    }

    private String createToken(Long userId, String username, String type, long expireMillis) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))          // sub：用户唯一标识
                .claim("username", username)               // 自定义 claim
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)                             // iat
                .expiration(new Date(now.getTime() + expireMillis)) // exp
                .signWith(key)                             // 默认 HS256
                .compact();
    }

    /**
     * 解析并验签。签名不对 / 已过期 / 格式错误 抛 JwtException
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)                           // 验签（0.12+ 新 API）
                .build()
                .parseSignedClaims(token)                  // 解析 + 校验 exp
                .getPayload();
    }

    /** 便捷方法：解析并校验 token 类型是否匹配 */
    public Claims parseAndCheckType(String token, String expectType) {
        Claims claims = parse(token);
        if (!expectType.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("token 类型不匹配，期望 " + expectType);
        }
        return claims;
    }

}

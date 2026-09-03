package com.knowledge.knowledgeagentplatform.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token 黑名单服务（注销功能的核心）
 *
 * 设计要点：
 *  - key = jwt:blacklist:{jti}，jti 是每个 token 的唯一 ID（JWT 标准 claim）
 *  - TTL = token 剩余有效期：token 自然过期后黑名单条目自动消失，不占 Redis 空间
 *  - 只拉黑 accessToken；refreshToken 由"删除 Redis 中存储的 refreshToken"实现作废
 *    （本 demo 的 refresh 是无状态的，真实项目 refresh 应入库/入 Redis）
 *
 * 为什么选黑名单而不是白名单：
 *  - 黑名单只影响"已注销"的少量 token，正常用户请求多查一次 Redis 而已
 *  - 白名单 = 每个 token 每次请求都依赖 Redis，JWT 退化成分布式 session，无状态优势丢失
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private  static final  String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void block(String jti, Duration ttl){
       if(jti == null || ttl == null || ttl.isNegative() || ttl.isZero()){
           return;
       }
       stringRedisTemplate.opsForValue().set(KEY_PREFIX+jti,"1",ttl);
    }


    /**
     * 拦截器每请求调用：判断 jti 是否已被拉黑
     *
     * <p>失败策略（fail-open）：Redis 不可用时返回 false，<b>不抛异常</b>。
     * 把降级逻辑收敛在本方法内、调用方无感知的理由：
     *  <ul>
     *    <li>主链路（JWT 验签 + 过期校验）完全不依赖 Redis，本方法失败不影响鉴权主流程</li>
     *    <li>注销是"缩短 token 有效窗口"的功能而非门禁，故障窗口内已注销 token 可用到自然过期</li>
     *    <li>生产建议：此处加 Micrometer 计数器（counter("jwt.blacklist.degrade").increment()）触发告警</li>
     *  </ul>
     */
    public boolean isBlocked(String jti){
        if(jti == null || jti.isBlank()) return false;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + jti));
        }catch (Exception e){
            // 降级放行：仅 Redis 故障，代码 bug 不要被这条 catch 吞掉
            log.warn("[JWT-BLACKLIST] Redis 不可用，降级放行，jti={}", jti, e);
            return false;
        }
    }
}

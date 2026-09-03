package com.knowledge.knowledgeagentplatform.security;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * RefreshToken 有状态存储（双 token 体系的"撤销把手"）
 * <p>
 * 设计要点：
 * - key = auth:refresh:{userId}，value = 当前有效的 refreshToken 的 jti
 * - 单设备模式：新登录覆盖旧值，旧设备的 refreshToken 自动失效
 * （真实项目多设备共存：key 加 deviceId 维度 → auth:refresh:{userId}:{deviceId}）
 * - TTL = refreshToken 有效期：与 token 自身 exp 双保险，谁先到都算过期
 * <p>
 * 为什么 accessToken 不存、refreshToken 必须存：
 * - access 每个请求都用，走无状态保吞吐；refresh 几天用一次，
 * 有状态的成本可忽略，换来的是"可撤销"——改密码/封号/盗号处置全靠删这条记录
 * <p>
 * 失败策略与黑名单刻意相反（fail-close）：
 * - 黑名单是增值功能：Redis 挂了降级放行（fail-open），接口不能全死
 * - refresh 轮换是撤销机制的关键路径：Redis 挂了就拒绝刷新（fail-close），
 * 用户稍后重试即可，手里的 accessToken（2h 内）不受影响
 * - 判断标准始终是一个问题：该功能失效时，"放行"和"拒绝"哪个更安全？
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    //设置前缀
    private final String KEY_PREFIX = "auth:refresh:";

    private StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存储
     *
     * @param userId
     * @param jti
     * @param duration
     */
    public void store(Long userId, String jti, Duration duration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, jti, duration);
    }

    /**
     * 删除
     *
     * @param userId
     */
    public void revoke(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    /**
     * 刷新时候轮换
     * @param userId
     * @param oldJti
     * @param newJti
     * @param ttl
     */
    public void rotate(Long userId,String oldJti,String newJti,Duration ttl){
         String key = KEY_PREFIX + userId;
        String s = redisTemplate.opsForValue().get(key);
        if(s == null || s.isBlank()){
            throw new BusinessException(401,"登录已过期，请重新登录");
        }
        if(!oldJti.equals(s)){
            log.warn("[REFRESH-ROTATE] jti 不匹配，疑似并发刷新或泄漏，撤销会话 userId={}", userId);
            redisTemplate.delete(key);
            throw new BusinessException(401, "refreshToken 已被使用，请重新登录");
        }
        //这两步其实一般情况下要使用lua脚本保证原子性
        redisTemplate.opsForValue().set(key,newJti,ttl);
    }

}

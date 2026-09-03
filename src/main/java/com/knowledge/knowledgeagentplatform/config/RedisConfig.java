package com.knowledge.knowledgeagentplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Redis 序列化配置
 *
 * 为什么要写这个类：
 *  - StringRedisTemplate 自带字符串序列化（黑名单用它，存的 "1"，永远不乱码）
 *  - 但要存对象（DTO/实体）就必须用 RedisTemplate<String, Object>，
 *    而 RedisTemplate 的默认序列化是 JdkSerializationRedisSerializer——
 *    key/value 在 redis-cli 里全是 \xac\xed\x00\x05 开头的二进制乱码，还没法跨服务读
 *
 * Boot 4 专项注意（网上老教程全是坑）：
 *  1. GenericJackson2JsonRedisSerializer（带 2）在 Spring Data Redis 4.0 已 @Deprecated，
 *     新类是 GenericJacksonJsonRedisSerializer（Jackson 3）
 *  2. 新类没有无参构造！new GenericJacksonJsonRedisSerializer() 直接编译报错，必须走 builder
 *  3. Jackson 3 包名从 com.fasterxml.jackson.* 改成了 tools.jackson.*，导包别导错
 *  4. 老类默认开 default typing（JSON 里写 @class）；新类默认关闭——
 *     不开的话 RedisTemplate<String, Object> 读回来是 LinkedHashMap，强转实体直接 ClassCastException
 */
@Configuration
public class RedisConfig {
    /**
     * 通用 RedisTemplate：key 字符串、value JSON
     * bean 名叫 redisTemplate，覆盖自动配置的同名 bean（默认 JDK 序列化那个）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        /*
         * value 序列化器：Jackson 3 JSON
         *
         * default typing 必须开（否则读回来是 LinkedHashMap），但开启有反序列化 RCE 风险——
         * 攻击面：能写 Redis 的人塞一个恶意 @class，应用读取时实例化 gadget chain。
         * 所以必须配 PolymorphicTypeValidator 白名单，只放行自己的包 + JDK 基础类型。
         * （绝对不要用 enableUnsafeDefaultTyping()，等价于 Jackson 2 时代的 LaissezFaireSubTypeValidator）
         */
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                //com.knowledge.knowledgeagentplatform
//                .allowIfSubType("com.example.jwtdemo.")   // 只信任自己的业务包
                .allowIfSubType("com.knowledge.knowledgeagentplatform.")
                .allowIfSubType("java.util.")             // ArrayList / HashMap 等集合
                .allowIfSubType("java.lang.")             // String / Integer 等
                .build();

        RedisSerializer<Object> jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(ptv)
                .build();

        // key 一律用字符串序列化，保证 redis-cli 里肉眼可读、跨语言可解析
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /*
     * StringRedisTemplate 不用配：自动配置就有，且自带 StringRedisSerializer。
     * 黑名单 TokenBlacklistService 继续用它——存取都是字符串，语义也更清晰。
     *
     * 用法约定：
     *  - 存字符串/简单标记（黑名单、验证码、计数器）→ StringRedisTemplate
     *  - 存对象（用户会话、缓存实体）              → redisTemplate（本类定义的）
     */

}

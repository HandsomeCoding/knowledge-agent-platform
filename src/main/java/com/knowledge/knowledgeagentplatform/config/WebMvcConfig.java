package com.knowledge.knowledgeagentplatform.config;

import com.knowledge.knowledgeagentplatform.common.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册 + 路径白名单
 *
 * excludePathPatterns 里的路径不需要 token：
 *  - /api/auth/**  登录、刷新 token（本身就是要拿 token 的入口）
 *  - swagger 相关  文档页面放行（生产环境建议关掉 springdoc 再讨论）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")          // 拦截范围：所有业务接口
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/register",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );
    }
}

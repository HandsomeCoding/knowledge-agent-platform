package com.knowledge.knowledgeagentplatform.common.interceptor;

import com.knowledge.knowledgeagentplatform.common.context.UserContext;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor  implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader(AUTH_HEADER);

        // 1. 头不存在或格式不对
        if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(401,"未登录或Token缺失");
        }

        String token = auth.substring(BEARER_PREFIX.length());

        // 2. 验签 + 过期校验 + 类型校验（refreshToken 不能调业务接口）
        try {
            Claims claims = jwtUtil.parseAndCheckType(token, JwtUtil.TYPE_ACCESS);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            UserContext.set(new UserContext.LoginUser(userId, username));
            return true;
        } catch (JwtException e) {
            // 过期（ExpiredJwtException）/ 签名不对 / 类型不匹配 都会到这
            throw new BusinessException(401,"token 无效或已过期：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(401,"token 载荷非法");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理：Tomcat 线程池复用线程，不清理会导致下个请求读到上个用户的身份（串号事故）
        UserContext.clear();
    }

}

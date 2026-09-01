package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.common.utils.JwtUtil;
import com.knowledge.knowledgeagentplatform.controller.param.LoginRequestParams;
import com.knowledge.knowledgeagentplatform.controller.vo.LoginResponseVo;
import com.knowledge.knowledgeagentplatform.controller.vo.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口：登录签发双 token、refreshToken 换新 accessToken
 *
 * demo 用内存写死的账号；真实项目把 checkPassword 换成
 * 查库 + BCryptPasswordEncoder.matches() 即可。
 *
 * 注意：登录失败 / refreshToken 无效都抛 BusinessException，
 * 让 GlobalExceptionHandler 统一包成 Result。Controller 只返回 success 路径。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<LoginResponseVo> login(@Valid @RequestBody LoginRequestParams req) {
        if (!"admin".equals(req.userName()) || !"123456".equals(req.password())) {
            throw new BusinessException("用户名或密码错误");
        }
        Long userId = 1L;
        return Result.success(new LoginResponseVo(
                jwtUtil.createAccessToken(userId, req.userName()),
                jwtUtil.createRefreshToken(userId, req.userName()),
                2 * 60 * 60,        // access 2h
                7 * 24 * 60 * 60  ,
                new UserInfo(1L,"admin","233333333","whwclear@163.com")// refresh 7d
        ));
    }

    /** refreshToken 换新 accessToken。真实场景建议 refreshToken 入库 + 单次使用作废 */
    @PostMapping("/refresh")
    public Result<LoginResponseVo> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("refreshToken 不能为空");   // 默认 code=400
        }
        try {
            Claims claims = jwtUtil.parseAndCheckType(refreshToken, JwtUtil.TYPE_REFRESH);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            return Result.success(new LoginResponseVo(
                    jwtUtil.createAccessToken(userId, username),
                    jwtUtil.createRefreshToken(userId, username),
                    2 * 60 * 60,        // access 2h
                    7 * 24 * 60 * 60  ,
                    new UserInfo(1L,"admin","233333333","whwclear@163.com")// refresh 7d
            ));
        } catch (JwtException e) {
            // refreshToken 也过期 → 引导重新登录
            throw new BusinessException(401, "refreshToken 无效或已过期，请重新登录");
        }
    }
}

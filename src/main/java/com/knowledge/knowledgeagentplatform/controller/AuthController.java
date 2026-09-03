package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.common.utils.JwtUtil;
import com.knowledge.knowledgeagentplatform.controller.param.AddUserParam;
import com.knowledge.knowledgeagentplatform.controller.param.LoginRequestParams;
import com.knowledge.knowledgeagentplatform.controller.vo.LoginResponseVo;
import com.knowledge.knowledgeagentplatform.controller.vo.UserInfo;
import com.knowledge.knowledgeagentplatform.security.RefreshTokenService;
import com.knowledge.knowledgeagentplatform.security.TokenBlacklistService;
import com.knowledge.knowledgeagentplatform.service.AppUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * 认证接口：登录签发双 token、refreshToken 换新 accessToken
 * <p>
 * demo 用内存写死的账号；真实项目把 checkPassword 换成
 * 查库 + BCryptPasswordEncoder.matches() 即可。
 * <p>
 * 注意：登录失败 / refreshToken 无效都抛 BusinessException，
 * 让 GlobalExceptionHandler 统一包成 Result。Controller 只返回 success 路径。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    private final TokenBlacklistService tokenBlacklistService;

    private final RefreshTokenService refreshTokenService;

    private final AppUserService appUserService;

    public AuthController(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, RefreshTokenService refreshTokenService,
                          AppUserService appUserService) {

        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenService = refreshTokenService;
        this.appUserService = appUserService;
    }

    @PostMapping("/login")
    public Result<LoginResponseVo> login(@Valid @RequestBody LoginRequestParams req) {
        if (!"admin".equals(req.userName()) || !"123456".equals(req.password())) {
            throw new BusinessException("用户名或密码错误");
        }
        Long userId = 1L;

        String refreshToken = jwtUtil.createRefreshToken(userId, req.userName());
        //保存refreshToken进入redis
        refreshTokenService.store(userId, jwtUtil.parse(refreshToken).getId(), jwtUtil.getRefreshExpireMillis());
        //登录成功保存
        return Result.success(new LoginResponseVo(
                jwtUtil.createAccessToken(userId, req.userName()),
                refreshToken,
                2 * 60 * 60,        // access 2h
                7 * 24 * 60 * 60,
                new UserInfo(1L, "admin", "233333333", "whwclear@163.com")// refresh 7d
        ));
    }

    /**
     * refreshToken 换新 accessToken。真实场景建议 refreshToken 入库 + 单次使用作废
     */
    @PostMapping("/refresh")
    public Result<LoginResponseVo> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("refreshToken 不能为空");   // 默认 code=400
        }
        try {
            Claims claims = jwtUtil.parseAndCheckType(refreshToken, JwtUtil.TYPE_REFRESH);
            //校验是否在黑名单
            if (tokenBlacklistService.isBlocked(claims.getId())) {
                throw new BusinessException(401, "当前用户token已过期,请重新登录");
            }
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            return Result.success(new LoginResponseVo(
                    jwtUtil.createAccessToken(userId, username),
                    jwtUtil.createRefreshToken(userId, username),
                    2 * 60 * 60,        // access 2h
                    7 * 24 * 60 * 60,
                    new UserInfo(1L, "admin", "233333333", "whwclear@163.com")// refresh 7d
            ));
        } catch (JwtException e) {
            // refreshToken 也过期 → 引导重新登录
            throw new BusinessException(401, "refreshToken 无效或已过期，请重新登录");
        }
    }

    /**
     * 登录注销
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或请求头缺失");
        }
        Claims claims = jwtUtil.parseAndCheckType(authorization.substring("Bearer ".length()), JwtUtil.TYPE_ACCESS);
        long time = claims.getExpiration().getTime();
        //accessToken加入黑名单
        tokenBlacklistService.block(claims.getId(), Duration.ofMillis(time));
        return Result.success("注销成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid AddUserParam addUserParam) {
        appUserService.addUser(addUserParam);
        return Result.success("用户注册成功");
    }
}

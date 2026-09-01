package com.knowledge.knowledgeagentplatform.common.context;

/**
 * 用户上下文：拦截器解析 token 后把用户信息放进 ThreadLocal，
 * 业务代码任意位置通过 UserContext.getUserId() 直接拿当前登录用户，
 * 免去层层传参。请求结束时拦截器负责 remove，防止内存泄漏 + 线程池串号。
 */
public class UserContext {

    public record LoginUser(Long userId, String username) {}

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    public static void clear() {
        HOLDER.remove();
    }

}

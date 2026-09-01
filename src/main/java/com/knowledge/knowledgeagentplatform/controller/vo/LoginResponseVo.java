package com.knowledge.knowledgeagentplatform.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录返回数据
 * record记录类
 */

public record LoginResponseVo (
    @Schema(description = "访问令牌，短期有效，用于业务接口请求头 Authorization: Bearer ...")
     String accessToken,

    @Schema(description = "刷新令牌，长期有效，用于 accessToken 过期时换新")
     String refreshToken,

    @Schema(description = "accessToken 剩余有效时间，单位：秒")
     long accessExpiresIn,

    @Schema(description = "refreshToken 剩余有效时间，单位：秒")
     long refreshExpiresIn,

    @Schema(description = "登录用户基本信息")
    UserInfo userInfo

){}

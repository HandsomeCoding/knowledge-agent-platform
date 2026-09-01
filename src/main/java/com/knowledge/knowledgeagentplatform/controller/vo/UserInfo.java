package com.knowledge.knowledgeagentplatform.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 纪录类
 */
public record UserInfo(

        @Schema(description = "用户id")
        Long id,
        @Schema(description = "用户名")
        String userName,
        @Schema(description = "用户电话")
        String usrPhone,
        @Schema(description = "用户邮箱")
        String userEmail
) {
}

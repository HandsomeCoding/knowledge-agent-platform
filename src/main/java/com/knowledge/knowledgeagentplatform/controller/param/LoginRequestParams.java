package com.knowledge.knowledgeagentplatform.controller.param;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestParams(
        @NotBlank(message = "用户名不能为空")
        @Schema(description = "用户登录名", example = "zhangsan", required = true)
        String userName,

        @Schema(description = "用户登录密码", example = "zhangsan", required = true)
        @NotBlank(message = "密码不能为空")
        String password
) {
}

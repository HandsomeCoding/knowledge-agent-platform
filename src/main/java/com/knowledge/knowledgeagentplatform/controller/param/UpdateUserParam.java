package com.knowledge.knowledgeagentplatform.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 修改用户请求参数
 */
public record UpdateUserParam(
        @NotNull(message = "用户id不能为空")
        Long id,

        @NotBlank(message = "用户名不能为空")
        String userName,

        @NotBlank(message = "电话不能为空")
        @Pattern(regexp = "^1[3-9]\\\\d{9}$", message = "电话格式不正确")
        String usrPhone,

        //允许空串
        @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$", message = "邮箱格式不正确")
        String userEmail

) {
}

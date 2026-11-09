package com.knowledge.knowledgeagentplatform.controller.param;


import jakarta.validation.constraints.NotNull;

public record RemovesReqParam(
        @NotNull(message = "删除id数据不能为空")
        Long[] ids
) {
}

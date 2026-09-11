package com.knowledge.knowledgeagentplatform.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AddDocumentParam(
        @NotNull(message = "知识库id不能为空")
        Long knowledgeBaseId,

        @NotBlank(message = "文件名不能为空")
        String documentName,


        @NotBlank(message = "文件内容不能为空")
        String documentContent


) {
}

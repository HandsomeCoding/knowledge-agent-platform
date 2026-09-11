package com.knowledge.knowledgeagentplatform.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDocumentParam(

        @NotNull(message = "文档id不能为空")
        Long id,

        @NotBlank(message = "文档内容不能为空")
        String documentContent,

        @NotBlank(message = "文件名不能为空")
        String documentName,

        @NotNull(message = "知识库id不能为空")
        Long knowledgeBaseId
) {
}

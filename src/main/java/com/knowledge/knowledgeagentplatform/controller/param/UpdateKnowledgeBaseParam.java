package com.knowledge.knowledgeagentplatform.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UpdateKnowledgeBaseParam(
        @NotNull(message = "文档归属人id不能为空")
        Long id,

        @NotNull(message = "文档归属人id不能为空")
        Long belongId,

        @NotBlank(message = "知识库名不能为空")
        @Length(max = 128, message = "知识库名最大长度为128")
        String knowledageName

) {
}

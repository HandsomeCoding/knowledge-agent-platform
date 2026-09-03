package com.knowledge.knowledgeagentplatform.controller.vo;

import com.knowledge.knowledgeagentplatform.entity.KnowledgeBase;
import io.swagger.v3.oas.annotations.media.Schema;

public record KnowledgeBaseVo(
        @Schema(description = "知识库id")
        Long id,

        @Schema(description = "知识库名称")
        String knowledgeName,

        @Schema(description = "知识库归属人id")
        Long belongId

) {
    public static KnowledgeBaseVo from(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseVo(knowledgeBase.getId(), knowledgeBase.getKnowledgeName(), knowledgeBase.getBelongId());
    }
}

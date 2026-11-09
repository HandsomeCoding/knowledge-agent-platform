package com.knowledge.knowledgeagentplatform.controller.vo;

import com.knowledge.knowledgeagentplatform.entity.Document;

public record DocumentBaseVo(
        Long id,


        String documentName,

        String documentPath,

        Long knowledgeBaseId,

        String documentType


) {
    public static DocumentBaseVo from(Document document) {
        return new DocumentBaseVo(document.getId(), document.getDocumentName(),
                document.getDocumentPath(), document.getKnowledgeBaseId(), document.getDocumentType());
    }
}

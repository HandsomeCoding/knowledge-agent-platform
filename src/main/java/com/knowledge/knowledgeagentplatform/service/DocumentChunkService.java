package com.knowledge.knowledgeagentplatform.service;

import com.knowledge.knowledgeagentplatform.entity.DocumentChunk;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
public interface DocumentChunkService extends IService<DocumentChunk> {

    void saveDocumentChunks(List<String> chunks,Long documentId);

    void updateDocumentChunks(List<String> chunks,Long documentId);

    void rebuildDocumentChunks(List<String> chunks,Long documentId);

}

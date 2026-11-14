package com.knowledge.knowledgeagentplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.entity.DocumentChunk;
import com.knowledge.knowledgeagentplatform.mapper.DocumentChunkMapper;
import com.knowledge.knowledgeagentplatform.service.DocumentChunkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@Service
public class DocumentChunkServiceImpl extends ServiceImpl<DocumentChunkMapper, DocumentChunk> implements DocumentChunkService {


    private final DocumentChunkMapper documentChunkMapper;

    public DocumentChunkServiceImpl(DocumentChunkMapper documentChunkMapper) {
        this.documentChunkMapper = documentChunkMapper;
    }

    /**
     * 保存数据下切片
     *
     * @param chunks
     * @param documentId
     */
    @Override
    public void saveDocumentChunks(List<String> chunks, Long documentId) {
        if (chunks == null || chunks.isEmpty())
            throw new BusinessException("文档id为 " + documentId + "下面的文件分片为空");
        List<DocumentChunk> documentChunks = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk documentChunk = new DocumentChunk();
            documentChunk.setContent(chunks.get(i));
            documentChunk.setChunkSum(chunks.size());
            documentChunk.setChunkSort(i + 1);
            documentChunk.setDocumentId(documentId);
            documentChunks.add(documentChunk);
        }
        this.saveBatch(documentChunks);

    }



    @Override
    public void updateDocumentChunks(List<String> chunks, Long documentId) {

        if (chunks == null || chunks.isEmpty())
            throw new BusinessException("文档id为 " + documentId + "下面的文件分片为空");
        List<DocumentChunk> documentChunks = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk documentChunk = new DocumentChunk();
            documentChunk.setContent(chunks.get(i));
            documentChunk.setChunkSum(chunks.size());
            documentChunk.setChunkSort(i + 1);
            documentChunk.setDocumentId(documentId);
            documentChunks.add(documentChunk);
        }
        this.saveBatch(documentChunks);
        //删除之前旧版本
        this.lambdaUpdate().eq(DocumentChunk::getDocumentId,documentChunks)
                .remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildDocumentChunks(List<String> chunks, Long documentId) {
        if (chunks == null || chunks.isEmpty())
            throw new BusinessException("文档id为 " + documentId + "下面的文件分片为空");
        //先删除旧数据
        documentChunkMapper.delete(new LambdaQueryWrapper<DocumentChunk>()
                .eq(DocumentChunk::getDocumentId, documentId));
        List<DocumentChunk> documentChunks = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk documentChunk = new DocumentChunk();
            documentChunk.setContent(chunks.get(i));
            documentChunk.setChunkSum(chunks.size());
            documentChunk.setChunkSort(i + 1);
            documentChunk.setDocumentId(documentId);
            documentChunks.add(documentChunk);
        }
        documentChunkMapper.insert(documentChunks);
    }


}

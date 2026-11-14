package com.knowledge.knowledgeagentplatform.service.impl;

import com.knowledge.knowledgeagentplatform.common.enums.ProcessStatus;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.utils.DocumentSplitUtils;
import com.knowledge.knowledgeagentplatform.entity.Document;
import com.knowledge.knowledgeagentplatform.mapper.DocumentChunkMapper;
import com.knowledge.knowledgeagentplatform.mapper.DocumentMapper;
import com.knowledge.knowledgeagentplatform.service.DocumentChunkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件上传处理类
 */
@Service
@Slf4j
public class ProcessService {

    private MinIOFileStorageService minIOFileStorageService;

    private DocumentMapper documentMapper;

    private DocumentChunkMapper documentChunkMapper;

    private DocumentChunkService documentChunkService;


    public ProcessService(MinIOFileStorageService minIOFileStorageService, DocumentMapper documentMapper,
                          DocumentChunkMapper documentChunkMapper,
                          DocumentChunkService documentChunkService) {
        this.documentChunkMapper = documentChunkMapper;
        this.documentMapper = documentMapper;
        this.minIOFileStorageService = minIOFileStorageService;
        this.documentChunkService = documentChunkService;
    }

    /**
     * 异步解析  切片 保存数据
     *
     * @param docId
     * @param userId
     * @return
     */
    @Async("docProcessExecutor")
    public void process(Long docId, Long userId) {
        Document document = documentMapper.selectById(docId);
        if (document == null) throw new BusinessException("id为" + docId + "文件不存在");
        //得到文件原文
        try {
            String content = extractText(document.getDocumentPath(), document.getDocumentName());
            document.setDocumentContent(content);
            //切片
            List<String> split = DocumentSplitUtils.split(content);
            //保存切片数据
            documentChunkService.rebuildDocumentChunks(split, docId);
            //更改
            markFinished(document);
        } catch (Exception e) {
            log.error("文件id为 " + docId + "切片报错。报错信息为" + e.getMessage());
            markFailed(document, e.getMessage());
        }

    }

    @Async("docProcessExecutor")
    public void rebuildchunks(String content, Long docId) {
        Document document = documentMapper.selectById(docId);
        if (document == null) throw new BusinessException("id为" + docId + "文件不存在");
        try {
            //切片
            List<String> split = DocumentSplitUtils.split(content);
            documentChunkService.rebuildDocumentChunks(split, docId);
            markFinished(document);
        } catch (Exception e) {
            log.error("文件id为 " + docId + "切片报错。报错信息为" + e.getMessage());
            markFailed(document, e.getMessage());
        }
    }


    private String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标记成功
     *
     * @param document
     */
    public void markFinished(Document document) {
        document.setProcessStatus(ProcessStatus.COMPLETED);
        //对正文进行hash
        document.setContentHash(md5(document.getDocumentContent()));
        documentMapper.updateById(document);
    }

    /**
     * 标记失败  并且更新失败相关原因
     *
     * @param document
     * @param msg
     */
    public void markFailed(Document document, String msg) {
        document.setProcessStatus(ProcessStatus.FAILED);
        document.setErrorMsg(msg);
        documentMapper.updateById(document);
    }


    /**
     * 获取文件
     *
     * @param documentPath
     * @param documentName
     * @return
     */
    public String extractText(String documentPath, String documentName) {
        try (InputStream in = minIOFileStorageService.getInputStream(documentPath)) {
            // -1 = 不限制提取长度（默认 10 万字符会截断长文档，知识库场景必须放开）
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, documentName);

            parser.parse(in, handler, metadata);
            String raw = handler.toString();

            // 扫描件/纯图片 PDF：解析成功但无文字层
            if (raw == null || raw.isBlank()) {
                throw new BusinessException(400,
                        "无法提取文本（可能是扫描件或图片型文件），请上传文字版或改用新建文档");
            }

            // 清洗后返回，调用方直接拿去切片：TextSplitter.split(text)
            return raw;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件解析失败: " + e.getMessage());
        }
    }
}

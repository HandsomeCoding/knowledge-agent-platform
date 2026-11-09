package com.knowledge.knowledgeagentplatform.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.knowledge.knowledgeagentplatform.controller.param.AddDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.param.RemovesReqParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentBaseVo;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentInfoVo;
import com.knowledge.knowledgeagentplatform.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
public interface DocumentService extends IService<Document> {

    void uploadEditDocument(AddDocumentParam addDocumentParam);


    void uploadDocument(MultipartFile multipartFile, Long knowledgeId);


    void updateDocument(UpdateDocumentParam updateDocumentParam);

    void removeDocument(RemovesReqParam removesReqParam);

    List<DocumentBaseVo> getDocumentByKnowledgeId(Long knowledgeId);

    DocumentInfoVo getDocumentById(Long id);
}

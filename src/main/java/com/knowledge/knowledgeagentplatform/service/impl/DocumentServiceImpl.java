package com.knowledge.knowledgeagentplatform.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.knowledge.knowledgeagentplatform.common.context.UserContext;
import com.knowledge.knowledgeagentplatform.common.enums.DeletedStatusEmuns;
import com.knowledge.knowledgeagentplatform.common.enums.DocumentSourceType;
import com.knowledge.knowledgeagentplatform.common.enums.ProcessStatus;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.utils.BeanCopyUtil;
import com.knowledge.knowledgeagentplatform.common.utils.DocumentSplitUtils;
import com.knowledge.knowledgeagentplatform.controller.param.AddDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.param.RemovesReqParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentBaseVo;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentInfoVo;
import com.knowledge.knowledgeagentplatform.entity.Document;
import com.knowledge.knowledgeagentplatform.entity.DocumentChunk;
import com.knowledge.knowledgeagentplatform.entity.KnowledgeBase;
import com.knowledge.knowledgeagentplatform.mapper.DocumentMapper;
import com.knowledge.knowledgeagentplatform.service.DocumentChunkService;
import com.knowledge.knowledgeagentplatform.service.DocumentService;
import com.knowledge.knowledgeagentplatform.service.FileStorageService;
import com.knowledge.knowledgeagentplatform.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {


    private final DocumentMapper documentMapper;

    private final DocumentChunkService documentChunkService;


    private final DocumentProcessService documentProcessService;


    private final FileStorageService fileStorageService;


    private final KnowledgeBaseService knowledgeBaseService;

    public DocumentServiceImpl(DocumentMapper documentMapper, DocumentChunkService documentChunkService,
                               DocumentProcessService documentProcessService,
                               FileStorageService fileStorageService,
                               KnowledgeBaseService knowledgeBaseService) {
        this.documentMapper = documentMapper;
        this.documentChunkService = documentChunkService;
        this.documentProcessService = documentProcessService;
        this.fileStorageService = fileStorageService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadEditDocument(AddDocumentParam addDocumentParam) {
        //文件名校验？  感觉没必要
        //不校验的话那就准备数据
        Document document = new Document();
        BeanCopyUtil.copyProperty(addDocumentParam, document);
        document.setSourceType(DocumentSourceType.ONLINE);
        document.setProcessStatus(ProcessStatus.COMPLETED);
        document.setContentVersion(1);
        //获取文件尾
        String name = addDocumentParam.documentName();
        int index = name.lastIndexOf(".");
        document.setDocumentType(name.substring(index));
        document.setDocumentName(name.substring(0, index));
        this.save(document);
        //content切片
        documentChunkService.saveDocumentChunks(DocumentSplitUtils.split(addDocumentParam.documentContent()), document.getId());


    }

    /**
     * 第一版存储数据到本地，暂定不使用异步
     *
     * @param multipartFile
     * @param knowledgeId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadDocument(MultipartFile multipartFile, Long knowledgeId) {
        if (multipartFile.isEmpty()) throw new BusinessException("文件不能为空");
        //上传文件到本地/OSS
        String filePath = fileStorageService.save(multipartFile);

        //解析文件得到数据
        String content = documentProcessService.extractText(multipartFile);

        //组装参数
        Document document = getDocument(multipartFile.getOriginalFilename(), knowledgeId, filePath);
        //保存参数和切片
        this.save(document);
        documentChunkService.saveDocumentChunks(DocumentSplitUtils.split(content), document.getId());

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDocument(UpdateDocumentParam updateDocumentParam) {

        //鉴权
        KnowledgeBase knowledgeBase = knowledgeBaseService.lambdaQuery().eq(KnowledgeBase::getId, updateDocumentParam.knowledgeBaseId())
                .eq(KnowledgeBase::getBelongId, UserContext.getUserId()).one();

        if (knowledgeBase == null) throw new BusinessException(403,"文档不存在或无权访问");
        //校验文档可存在
        Document document = this.lambdaQuery().eq(Document::getId, updateDocumentParam.id())
                .eq(Document::getKnowledgeBaseId, updateDocumentParam.knowledgeBaseId())
                .one();
        if (document == null) throw new BusinessException("文档不存在或者当前知识库下没有该文档");
        //校验当前版本hash 一样说明不需要修改
        String normalize = DocumentSplitUtils.normalize(updateDocumentParam.documentContent());
        String newHash = md5(normalize);
        Boolean nameChanged = updateDocumentParam.documentName() == null || !updateDocumentParam.documentName().isBlank()
                || updateDocumentParam.documentName().equals(document.getDocumentName());
        if (newHash.equals(document.getContentHash()) && nameChanged) {
            return;
        }
        boolean update = this.lambdaUpdate()
                .set(Document::getDocumentName, nameChanged ? document.getDocumentName() : updateDocumentParam.documentName())
                .set(Document::getDocumentContent, normalize)
                .set(Document::getContentHash, newHash)
                .set(Document::getProcessStatus, ProcessStatus.COMPLETED)
                .eq(Document::getId, updateDocumentParam.id())
                .eq(Document::getKnowledgeBaseId, UserContext.getUserId()).update();

        if (!update) {
            throw new BusinessException(404, "文档不存在或无权操作");
        }
        //hash改变了再重新生成切片
        if (!newHash.equals(document.getContentHash())) {
            documentChunkService.updateDocumentChunks(DocumentSplitUtils.split(updateDocumentParam.documentContent()),
                    updateDocumentParam.id());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDocument(RemovesReqParam removesReqParam) {
        if (removesReqParam.ids() == null || removesReqParam.ids().length == 0)
            throw new BusinessException("传入的文档id集合不能为空");
        Long[] ids = removesReqParam.ids();

        //文档物理删除
        //归属人,需不需要加？后面在考虑
        this.lambdaUpdate().in(Document::getId, Arrays.asList(ids))
                .set(Document::getDeleted, DeletedStatusEmuns.DELETED.getCode())
                .update();
        //切片直接删除
        documentChunkService.lambdaUpdate()
                .in(DocumentChunk::getDocumentId, Arrays.asList(ids))
                .remove();
    }

    @Override
    public List<DocumentBaseVo> getDocumentByKnowledgeId(Long knowledgeId) {
        if (knowledgeId == null) throw new BusinessException("基础数据库id为空");
        //鉴权
        KnowledgeBase knowledgeBase = knowledgeBaseService.lambdaQuery().eq(KnowledgeBase::getId, knowledgeId)
                .eq(KnowledgeBase::getBelongId, UserContext.getUserId()).one();

        if (knowledgeBase == null) throw new BusinessException(403,"知识库不存在或者权限不足");
        List<Document> list = this.lambdaQuery().eq(Document::getKnowledgeBaseId, knowledgeId)
                .list();
        return list.stream().map(DocumentBaseVo::from).toList();
    }

    @Override
    public DocumentInfoVo getDocumentById(Long id) {
        if (id == null) throw new BusinessException("文档id为空");


        Document document = this.getById(id);
        if (document == null) throw new BusinessException("当前文档不存在或者权限不足");


        //鉴权
        KnowledgeBase knowledgeBase = knowledgeBaseService.lambdaQuery().eq(KnowledgeBase::getId, document.getKnowledgeBaseId())
                .eq(KnowledgeBase::getBelongId, UserContext.getUserId()).one();
        if (knowledgeBase == null) throw new BusinessException(403,"知识库不存在或者权限不足");

        DocumentInfoVo documentInfoVo = new DocumentInfoVo();
        BeanCopyUtil.copyProperty(document, documentInfoVo);
        return documentInfoVo;
    }

    /**
     * 拼装Document参数
     *
     * @param fileName
     * @param knowledgeId
     * @param filePath
     * @return
     */
    private static Document getDocument(String fileName, Long knowledgeId, String filePath) {
        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeId);
        document.setSourceType(DocumentSourceType.UPLOAD);
        document.setProcessStatus(ProcessStatus.COMPLETED);
        //获取文件尾
        if (fileName == null || fileName.isBlank()) throw new BusinessException("文件解析错误，文件名为空");
        int index = fileName.lastIndexOf(".");
        document.setDocumentType(fileName.substring(index));
        document.setDocumentName(fileName.substring(0, index));
        document.setDocumentPath(filePath);
        document.setContentVersion(1);
        return document;
    }


    /**
     * sha256 hash
     *
     * @param text
     * @return
     */
    private String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
}

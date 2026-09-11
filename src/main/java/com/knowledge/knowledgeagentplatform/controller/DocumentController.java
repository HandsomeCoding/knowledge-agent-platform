package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.controller.param.AddDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateDocumentParam;
import com.knowledge.knowledgeagentplatform.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/knowledgeagentplatform/document")
public class DocumentController {


    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    //上传可编辑文档

    @PostMapping("/uploadEditDocument")
    public Result<String> uploadEditDocument(@RequestBody AddDocumentParam addDocumentParam) {
        documentService.uploadEditDocument(addDocumentParam);
        return Result.success("保存成功");
    }

    //上传文件
    @PostMapping("/uploadDocument")
    public Result<String> uploadDocument(@RequestParam("multipartFile") MultipartFile multipartFile,
                                         @RequestParam("multipartFile") Long knowledgeId) {
        documentService.uploadDocument(multipartFile,knowledgeId);
        return Result.success("保存成功");
    }

    @PostMapping("/updateDocument")
    public Result<String> updateDocument(@RequestBody @Valid UpdateDocumentParam updateDocumentParam) {
        documentService.updateDocument(updateDocumentParam);
        return Result.success("修改成功");
    }
    //修改

    //删除
    //查询知识库下所有的document
    //根据docUmentId查详情  全返回还是部分返回？


}

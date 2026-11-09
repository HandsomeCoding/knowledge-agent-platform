package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.controller.param.AddDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.param.RemovesReqParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateDocumentParam;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentBaseVo;
import com.knowledge.knowledgeagentplatform.controller.vo.DocumentInfoVo;
import com.knowledge.knowledgeagentplatform.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @PostMapping("/removeDocument")
    public Result<String> removeDocument(@RequestBody @Valid RemovesReqParam removesReqParam){
        documentService.removeDocument(removesReqParam);
        return Result.success("删除成功");
    }
    //查询知识库下所有的document

    @GetMapping("/getDocumentByKnowledgeId")
    public Result<List<DocumentBaseVo>> getDocumentByKnowledgeId(@RequestParam("knowledgeBaseId") Long knowledgeBaseId){
        return Result.success(documentService.getDocumentByKnowledgeId(knowledgeBaseId));
    }
    //根据docUmentId查详情  全返回还是部分返回？

    @GetMapping("/getDocumentById")
    public Result<DocumentInfoVo> getDocumentById(@RequestParam("id") Long id){
        return Result.success(documentService.getDocumentById(id));
    }


}

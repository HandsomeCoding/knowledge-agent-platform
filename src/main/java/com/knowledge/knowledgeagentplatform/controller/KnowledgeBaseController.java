package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.controller.param.AddKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.vo.KnowledgeBaseVo;
import com.knowledge.knowledgeagentplatform.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/knowledgeagentplatform/knowledgeBase")
@Validated
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/addKnowledgeBase")
    public Result<String> addKnowledgeBase(@Valid @RequestBody AddKnowledgeBaseParam addKnowledgeBaseParam) {
        knowledgeBaseService.addKnowledgeBase(addKnowledgeBaseParam);
        return Result.success("知识库新增成功");
    }

    @PostMapping("/updateKnowledgeBase")
    public Result<String> updateKnowledgeBase(@Valid @RequestBody UpdateKnowledgeBaseParam updateKnowledgeBaseParam) {
        knowledgeBaseService.updateKnowledgeBase(updateKnowledgeBaseParam);
        return Result.success("知识库修改成功");
    }

    /**
     * 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/getAllKnowledgeBase")
    public Result<List<KnowledgeBaseVo>> getAllKnowledgeBase(@RequestParam(value = "id", required = true) Long id) {
        return Result.success(knowledgeBaseService.getAllKnowledgeBase(id));
    }


}

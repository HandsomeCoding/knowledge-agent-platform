package com.knowledge.knowledgeagentplatform.service;

import com.knowledge.knowledgeagentplatform.controller.param.AddKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.vo.KnowledgeBaseVo;
import com.knowledge.knowledgeagentplatform.entity.KnowledgeBase;
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
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    void addKnowledgeBase(AddKnowledgeBaseParam addKnowledgeBaseParam);

    void updateKnowledgeBase(UpdateKnowledgeBaseParam updateKnowledgeBaseParam);

    List<KnowledgeBaseVo> getAllKnowledgeBase(Long userId);

}

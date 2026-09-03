package com.knowledge.knowledgeagentplatform.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.knowledge.knowledgeagentplatform.common.context.UserContext;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.utils.BeanCopyUtil;
import com.knowledge.knowledgeagentplatform.controller.param.AddKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateKnowledgeBaseParam;
import com.knowledge.knowledgeagentplatform.controller.vo.KnowledgeBaseVo;
import com.knowledge.knowledgeagentplatform.entity.KnowledgeBase;
import com.knowledge.knowledgeagentplatform.mapper.KnowledgeBaseMapper;
import com.knowledge.knowledgeagentplatform.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {


    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public KnowledgeBaseServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addKnowledgeBase(AddKnowledgeBaseParam addKnowledgeBaseParam) {

        KnowledgeBase knowledgeBaseMapperByName = this.lambdaQuery().eq(KnowledgeBase::getBelongId, UserContext.getUserId())
                .eq(KnowledgeBase::getKnowledgeName, addKnowledgeBaseParam.knowledageName())
                .one();

        if (knowledgeBaseMapperByName != null) {
            throw new BusinessException("知识库名重复,请重新输入");
        }
        knowledgeBaseMapperByName = new KnowledgeBase();
        BeanCopyUtil.copyProperty(addKnowledgeBaseParam, knowledgeBaseMapperByName);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateKnowledgeBase(UpdateKnowledgeBaseParam updateKnowledgeBaseParam) {
        boolean update = this.lambdaUpdate().set(KnowledgeBase::getKnowledgeName, updateKnowledgeBaseParam.knowledageName())
                .eq(KnowledgeBase::getBelongId, UserContext.getUserId())
                .eq(KnowledgeBase::getId, updateKnowledgeBaseParam.id())
                .update();
        if (!update) {
            throw new BusinessException("文档不存在或当前操作人无权操作");
        }
    }

    @Override
    public List<KnowledgeBaseVo> getAllKnowledgeBase(Long userId) {
        List<KnowledgeBase> list = this.lambdaQuery().eq(KnowledgeBase::getBelongId, userId)
                .list();
        return list.stream().map(KnowledgeBaseVo::from).toList();
    }


}

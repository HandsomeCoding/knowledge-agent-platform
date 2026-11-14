package com.knowledge.knowledgeagentplatform.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.knowledge.knowledgeagentplatform.common.enums.ProcessStatus;
import com.knowledge.knowledgeagentplatform.entity.Document;
import com.knowledge.knowledgeagentplatform.mapper.DocumentMapper;
import com.knowledge.knowledgeagentplatform.service.impl.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时器  解决中间段的数据 比如线程异常中断  markfailed没泡成的数据 还有就是防止毒数据一直循环 直接判定失败 等待后续认为重试
 * <p>
 * 确保数据只有两个终态  一个是完成  一个是失败 两个都是可以被程序感知的
 */
@Component
@Slf4j
public class DocTimeoutScheduler {

    private final DocumentMapper documentMapper;

    private ProcessService processService;

    public DocTimeoutScheduler(DocumentMapper documentMapper, ProcessService processService) {
        this.documentMapper = documentMapper;
        this.processService = processService;
    }

    @Scheduled(fixedDelay = 60_000)   // 上次跑完再等60s，不重叠
    public void rescueStuck() {
        List<Document> stuck = documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getProcessStatus, ProcessStatus.PROCESSING)
                        .lt(Document::getUpdateTime, LocalDateTime.now().minusMinutes(5)));

        for (Document doc : stuck) {
            if (doc.getRetryCount() >= 3) {
                documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                        .eq(Document::getId, doc.getId())
                        .set(Document::getProcessStatus, ProcessStatus.FAILED)
                        .set(Document::getErrorMsg, "重试次数耗尽"));
                log.warn("文档{}重试耗尽，标FAILED", doc.getId());
            } else {
                documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                        .eq(Document::getId, doc.getId())
                        .set(Document::getRetryCount, doc.getRetryCount() + 1));
                log.info("文档{}处理超时，第{}次重试", doc.getId(), doc.getRetryCount() + 1);
                processService.process(doc.getId(), null);   // 重触发
            }
        }
    }
}

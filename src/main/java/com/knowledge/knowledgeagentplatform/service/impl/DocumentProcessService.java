package com.knowledge.knowledgeagentplatform.service.impl;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.utils.DocumentSplitUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文档处理服务：解析（Tika）→ 清洗 → 切片 的编排层
 * <p>
 * V1 同步处理（文件小解析快，先保正确再优化）；
 * V2 演进点：@Async + status 状态机（解析中/就绪/失败），大文件不阻塞上传响应。
 * <p>
 * 扫描件防御：图片型 PDF 解析出空文本（无文字层），明确置为失败态而非静默入库——
 * "诚实失败"比"假装成功"好排查。
 */
@Service
public class DocumentProcessService {

    /**
     * 解析文件为纯文本
     *
     * @param file 上传的原文件
     * @return 清洗后的纯文本（可直接进切片器）
     */
    public String extractText(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            // -1 = 不限制提取长度（默认 10 万字符会截断长文档，知识库场景必须放开）
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());

            parser.parse(in, handler, metadata);
            String raw = handler.toString();

            // 扫描件/纯图片 PDF：解析成功但无文字层
            if (raw == null || raw.isBlank()) {
                throw new BusinessException(400,
                        "无法提取文本（可能是扫描件或图片型文件），请上传文字版或改用新建文档");
            }

            // 清洗后返回，调用方直接拿去切片：TextSplitter.split(text)
            return DocumentSplitUtils.normalize(raw);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 切片：输入已清洗文本，输出切片列表（供入库 knowledge_chunk）
     */
    public List<String> chunk(String cleanedText) {
        return DocumentSplitUtils.split(cleanedText);
    }


}

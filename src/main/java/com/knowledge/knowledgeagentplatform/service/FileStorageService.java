package com.knowledge.knowledgeagentplatform.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


/**
 * 文件存储抽象（档1：本地磁盘实现，档2 起换 MinIO/OSS 只加实现类不动调用方）
 *
 * 为什么抽接口：上传解析链路（Controller/DocumentService）只依赖"存/取/删"
 * 三个动作，不关心文件放哪——换 MinIO 时新增实现类，Controller 一行不改。
 */
public interface FileStorageService {
    /**
     * 保存上传文件
     *
     * @return 生成的存储相对路径（如 202609/{uuid}.pdf），落库用
     */
    String save(MultipartFile file);

    /** 按相对路径取文件的绝对路径（预览/解析时用） */
   String resolve(String relativePath);

    /** 删除文件（逻辑删除文档时清理物理文件用，静默失败可接受） */
    void delete(String relativePath);
}

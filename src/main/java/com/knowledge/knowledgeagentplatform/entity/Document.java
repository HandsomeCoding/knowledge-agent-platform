package com.knowledge.knowledgeagentplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@Getter
@Setter
@ToString
@TableName("document")
@Accessors(chain = true)
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 文件名
     */
    @TableField("document_name")
    private String documentName;

    /**
     * 文件位置
     */
    @TableField("document_path")
    private String documentPath;

    /**
     * 归属知识库id
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;

    /**
     * 文档处理状态
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 文档全文  目前支持16m
     */
    @TableField("document_content")
    private String documentContent;

    @TableLogic
    @TableField("deleted")// MP 识别逻辑删除字段
    private Integer deleted;
}

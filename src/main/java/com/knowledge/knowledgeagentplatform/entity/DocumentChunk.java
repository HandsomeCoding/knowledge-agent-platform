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
@Accessors(chain = true)
@TableName("document_chunk")
public class DocumentChunk implements Serializable {

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
     * 文件块个数
     */
    @TableField("chunk_sort")
    private String chunkSort;

    /**
     * 文件块内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件块总数
     */
    @TableField("chunk_sum")
    private String chunkSum;

    @TableLogic
    @TableField("deleted")// MP 识别逻辑删除字段
    private Integer deleted;
}

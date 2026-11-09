package com.knowledge.knowledgeagentplatform.controller.vo;

import com.knowledge.knowledgeagentplatform.common.enums.DocumentSourceType;
import com.knowledge.knowledgeagentplatform.common.enums.ProcessStatus;
import lombok.Data;

@Data
public class DocumentInfoVo {


    private Long id;


    private String documentName;


    private String documentPath;


    private Long knowledgeBaseId;


    private ProcessStatus processStatus;


    private String documentContent;


    private DocumentSourceType sourceType;


    private Integer contentVersion;


    private String documentType;


    private String contentHash;
}

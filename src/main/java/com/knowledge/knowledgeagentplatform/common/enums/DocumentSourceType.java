package com.knowledge.knowledgeagentplatform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import lombok.Getter;

/**
 * 文件来源
 */
@Getter
public enum DocumentSourceType {
    UPLOAD(1, "上传"),
    ONLINE(2, "在线编辑");

    @EnumValue
    private final int code;

    private final String desc;

    DocumentSourceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DocumentSourceType getByCode(int code) {
        for (DocumentSourceType value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new BusinessException(500, "未知的文档状态: " + code);
    }
}

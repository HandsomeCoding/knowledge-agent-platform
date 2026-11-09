package com.knowledge.knowledgeagentplatform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import lombok.Getter;

/**
 * 文件来源
 */
@Getter
public enum DeletedStatusEmuns {
    NORMAL(0, "未删除"),
    DELETED(1, "删除");

    @EnumValue
    private final int code;

    private final String desc;

    DeletedStatusEmuns(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeletedStatusEmuns getByCode(int code) {
        for (DeletedStatusEmuns value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new BusinessException(500, "未知的文档状态: " + code);
    }
}

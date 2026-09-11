package com.knowledge.knowledgeagentplatform.common.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum ProcessStatus {
    PENDING(2, "待处理"),
    PROCESSING(3, "处理中"),
    COMPLETED(1, "已完成"),
    FAILED(0, "失败");
    @EnumValue
    private final int code;

    private final String desc;

    ProcessStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessStatus getByCode(int code) {
        for (ProcessStatus value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new BusinessException(500, "未知的文档状态: " + code);
    }
}

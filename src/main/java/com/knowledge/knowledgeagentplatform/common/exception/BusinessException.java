package com.knowledge.knowledgeagentplatform.common.exception;


import lombok.Data;

/**
 * 定义业务异常
 */
@Data
public class BusinessException extends RuntimeException{
    private Integer code;

    public BusinessException(String msg){
        super(msg);
        this.code = 400;
    }

    public BusinessException(Integer code,String msg){
        super(msg);
        this.code = code;
    }
}

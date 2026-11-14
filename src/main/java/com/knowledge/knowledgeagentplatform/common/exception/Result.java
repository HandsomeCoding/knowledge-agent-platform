package com.knowledge.knowledgeagentplatform.common.exception;

import lombok.Data;

/**
 * 针对方法返回结果
 *  200  成功    400业务统一失败     401未登录 404查询不到   403无权
 *
 *  public interface ErrorCode {
 *     int BAD_REQUEST = 400;
 *     int UNAUTHORIZED = 401;
 *     int FORBIDDEN = 403;
 *     int NOT_FOUND = 404;
 * }
 * // 用：throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该文档");
 *
 * @param <T>
 */
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public Result (){}


    public Result (Integer code,String msg,T data){
        this.code =code;
        this.msg = msg;
        this.data = data;

    }

    //定义失败 成功的方法
    public static <T> Result<T> success(T data){
        return new Result<>(200,"操作成功",data);
    }


    public static <T> Result<T> fail(Integer code,String msg){
        return new Result<>(code,msg,null);
    }


}

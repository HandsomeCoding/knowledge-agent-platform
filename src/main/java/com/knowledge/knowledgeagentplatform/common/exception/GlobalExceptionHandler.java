package com.knowledge.knowledgeagentplatform.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常拦截处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 专门拦截 @RequestBody JSON 参数校验失败
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
        //取出第一个校验的异常返回给前端
        FieldError fieldError = e.getBindingResult().getFieldError();
        //数据处理
        String name ="";
//        Optional.ofNullable(name)
//                .orElseGet(()-> {return Result.fail(400,"参数校验失败");});
        String msg = fieldError !=null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.fail(400,msg);
    }

    /**
     * 处理url参数校验失败
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolation(ConstraintViolationException e){

        //对异常报错进行拼接返回给前端
        String collect = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        return Result.fail(400,collect);
    }



    /**
     * 处理业务异常
     * @param e
     * @return
     */
   @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e){
        return Result.fail(e.getCode(),e.getMessage());
    }



    /**
     * 处理系统中除去上面定义异常之外的所有异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e){
        //打印控制台方便调试，一般这里可以控制日志
        log.error(e.getMessage());
        return Result.fail(400,e.getMessage());
    }
}

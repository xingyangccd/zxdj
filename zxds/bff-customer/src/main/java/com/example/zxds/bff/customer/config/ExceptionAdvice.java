package com.example.zxds.bff.customer.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.json.JSONObject;
import com.example.zxds.common.exception.ZxdsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 基于 Spring Boot 的 @RestControllerAdvice 实现，统一拦截 Controller 层抛出的异常，
 * 并将其转换为 JSON 格式返回给前端，避免在每个接口中重复编写 try-catch。
 * <p>
 * 处理逻辑：
 * <ul>
 *     <li>MethodArgumentNotValidException：参数校验失败，返回具体校验错误信息</li>
 *     <li>HttpRequestMethodNotSupportedException：请求方法不支持当前类型</li>
 *     <li>HttpMessageNotReadableException：请求体缺失或格式错误</li>
 *     <li>ZxdsException：自定义业务异常，返回业务错误信息并记录日志</li>
 *     <li>其他未知异常：记录日志并返回通用错误提示</li>
 *     <li>NotLoginException：Sa-Token 未登录异常，返回 401 未授权状态</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        JSONObject json = new JSONObject();
        if (e instanceof MethodArgumentNotValidException exception) {
            json.set("error", exception.getBindingResult().getFieldError().getDefaultMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            json.set("error", "Web方法不支持当前的请求类型");
        } else if (e instanceof HttpMessageNotReadableException) {
            json.set("error", "缺少提交的数据");
        } else if (e instanceof ZxdsException exception) {
            log.error("执行异常", e);
            json.set("error", exception.getMsg());
        } else {
            log.error("执行异常", e);
            json.set("error", "执行异常");
        }
        return json.toString();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public String unLoginHandler(Exception e) {
        JSONObject json = new JSONObject();
        json.set("error", e.getMessage());
        return json.toString();
    }

}
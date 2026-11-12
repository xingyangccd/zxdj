package com.example.zxds.dr.config;

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

}
package com.demo.bff.config;

import com.demo.bff.common.BizException;
import com.demo.bff.common.ErrorCode;
import com.demo.bff.common.R;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 保持住下游抛出的异常信息。
    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // 当上游连接下游超时，或者下游读超时，都会发生这个异常，走这里。
    @ExceptionHandler(RetryableException.class)
    public R<Void> handleTimeout(RetryableException e) {
        log.error("Feign 调用超时", e);
        return R.fail(ErrorCode.DOWNSTREAM_TIMEOUT.code, ErrorCode.DOWNSTREAM_TIMEOUT.msg);
    }

    // 当 下游返回的HTTP状态码不是 2xx时兜底。（但大部分情况下，下游返回的HTTP状态码都是200，所以这个方法暂时写上，兜底作用）
    @ExceptionHandler(FeignException.class)
    public R<Void> handleFeign(FeignException e) {
        log.error("Feign 调用失败, status={}", e.status(), e);
        return R.fail(ErrorCode.DOWNSTREAM_ERROR.code, ErrorCode.DOWNSTREAM_ERROR.msg);
    }

    // 最终兜底
    @ExceptionHandler(Exception.class)
    public R<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return R.fail(ErrorCode.SYSTEM_ERROR.code, ErrorCode.SYSTEM_ERROR.msg);
    }
}
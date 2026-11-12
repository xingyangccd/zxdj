package com.demo.order.config;

import com.demo.order.common.BizException;
import com.demo.order.common.ErrorCode;
import com.demo.order.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 下游子系统当中凡是发生业务异常的，统一走这个处理。
    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务异常: code={}, msg={}", e.getErrorCode().code, e.getMessage());
        // 尽管下游出现了 BizException 异常，但是向上游BFF反馈的是一个 R 对象，HTTP状态码一定是200
        // e.getErrorCode().code 获取的是：业务错误码
        // e.getMessage() 获取的是：错误码描述信息
        return R.fail(e.getErrorCode().code, e.getMessage());
    }

    // 兜底。
    @ExceptionHandler(Exception.class)
    public R<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        // 尽管下游出现了 Exception 异常，但是向上游BFF反馈的是一个 R 对象，HTTP状态码一定是200
        return R.fail(ErrorCode.SYSTEM_ERROR.code, ErrorCode.SYSTEM_ERROR.msg);
    }
}
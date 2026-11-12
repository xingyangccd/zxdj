package com.demo.bff.common;

public enum ErrorCode {
    // 当全局异常处理器走最终兜底时这个枚举值就用到了。
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),

    // 当全局异常处理器走 FeignException 的时候，使用这个枚举值。
    DOWNSTREAM_ERROR(50002, "下游服务异常"),

    // 当全局异常处理器走 RetryableException 的时候，使用这个枚举值
    // 什么时候发生 RetryableException：当 connectTimeout: 500 以及 readTimeout: 1000 时都会使用这个枚举值。
    DOWNSTREAM_TIMEOUT(50003, "下游服务超时");

    public final int code;
    public final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
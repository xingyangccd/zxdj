package com.demo.order.common;

public enum ErrorCode {
    // 三个枚举值
    // 1.系统错误
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),
    // 2.订单不存在
    ORDER_NOT_FOUND(40004, "订单不存在"),
    // 3.参数错误
    PARAM_INVALID(40000, "参数错误");

    // 实际开发中肯定不止这三个，可以继续添加枚举值...

    // code仍然是业务码
    // msg仍然是业务码的描述信息
    public final int code;
    public final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
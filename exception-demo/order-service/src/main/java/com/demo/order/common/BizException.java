package com.demo.order.common;

// 系统中自定义的【业务异常】。
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    /*public BizException(String msg){
        super(msg);
    }*/

    // 构造业务异常时，参数传递枚举值。
    public BizException(ErrorCode errorCode) {
        // 将【业务码对应的错误描述信息】传递给 Throwable 的 detailMessage 属性了。
        // 最终调用异常对象的 e.getMessage() 方法是可以拿到这个错误描述信息的。
        super(errorCode.msg);
        this.errorCode = errorCode;
    }

    // getter方法。
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
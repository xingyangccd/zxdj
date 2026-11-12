package com.demo.bff.common;

public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.msg);
        this.code = errorCode.code;
    }

    public int getCode() {
        return code;
    }
}
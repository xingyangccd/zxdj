package com.example.zxds.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 自定义业务异常类
 * <p>
 * 作用：在项目中抛出业务相关的异常时使用，方便全局异常处理器统一捕获并返回。
 * <p>
 * 相比普通的 RuntimeException，多带了两个字段：
 * - msg：错误提示信息，用于返回给前端展示
 * - code：错误码，方便前端或调用方区分不同的错误类型
 * <p>
 * 提供了 4 个构造方法，方便不同场景使用：
 * 1. 只传错误信息，错误码默认为 500；
 * 2. 传错误信息 + 原始异常（用于保留异常链）；
 * 3. 传错误信息 + 自定义错误码；
 * 4. 传错误信息 + 自定义错误码 + 原始异常。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ZxdsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String msg;
    private final int code;

    public ZxdsException(String msg) {
        super(msg);
        this.msg = msg;
        this.code = 500;
    }

    public ZxdsException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = 500;
    }

    public ZxdsException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public ZxdsException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}
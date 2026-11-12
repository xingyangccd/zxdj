package com.demo.order.common;

import lombok.Data;

@Data
public class R<T> {
    // 业务码
    private int code;
    // 业务码的描述信息
    private String message;
    // 返回的具体数据
    private T data;

    // 成功
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    // 失败
    // code参数仍然是业务码
    // message参数仍然是业务码的描述信息
    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }
}
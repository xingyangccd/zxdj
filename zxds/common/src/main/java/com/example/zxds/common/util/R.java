package com.example.zxds.common.util;

import org.apache.hc.core5.http.HttpStatus;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果封装类
 * <p>
 * 作用：把后端接口的返回结果统一成固定格式，方便前端统一处理。
 * <p>
 * 本质是一个 HashMap，所以可以像 Map 一样往里面 put 任意数据。
 * 默认带有两个字段：
 * - code：状态码，默认 200（成功）
 * - msg：提示信息，默认 "success"
 * <p>
 * 常用方法：
 * - ok()：返回成功结果；
 * - ok(msg)：返回成功结果，并自定义提示信息；
 * - ok(map)：返回成功结果，并把 map 里的数据一并放入；
 * - error()：返回失败结果，默认 500 和"未知异常，请联系管理员"；
 * - error(msg)：返回失败结果，自定义提示信息，状态码 500；
 * - error(code, msg)：返回失败结果，自定义状态码和提示信息。
 * <p>
 * 重写了 put 方法，让它返回 R 本身，这样可以链式调用，
 * 比如：R.ok().put("data", userList)。
 */
public class R extends HashMap<String, Object> {

    @Serial
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

}
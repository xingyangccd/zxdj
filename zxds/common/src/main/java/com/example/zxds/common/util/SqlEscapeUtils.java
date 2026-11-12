package com.example.zxds.common.util;

/**
 * Phoenix SQL 注入防御工具类
 */
public class SqlEscapeUtils {

    /**
     * 转义 Phoenix SQL 中的字符串
     * 原理：将单引号 ' 替换为两个单引号 ''，过滤反斜杠
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        // 1. 过滤反斜杠，防止转义绕过
        String result = input.replace("\\", "\\\\");
        // 2. 将单引号替换为两个单引号（SQL 标准转义）
        result = result.replace("'", "''");
        return result;
    }
}
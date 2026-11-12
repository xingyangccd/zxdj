package com.example.zxds.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 全局 JSON 序列化配置类
 * <p>
 * 作用：统一控制后端返回给前端的 JSON 格式，主要解决两个问题：
 * <p>
 * 1. 精度丢失问题：
 *    JavaScript 的 Number 类型最大安全整数是 2^53-1，
 *    Java 的 Long 类型（雪花 ID 等）通常会超过这个范围，
 *    直接返回给前端会导致末位精度丢失（比如 ID 变了）。
 *    所以这里把 Long、Double、Float 都序列化成字符串返回。
 * <p>
 * 2. 空值冗余问题：
 *    值为 null 的字段默认也会出现在 JSON 里，
 *    这里设置为 NON_NULL，null 字段直接不输出，减小响应体积。
 */
@JsonComponent
public class JsonSerializerManage {

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // 忽略 value 为 null 时 key 的输出
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule module = new SimpleModule();
        // 长整型和浮点型在 JavaScript 上面都会丢失精度，所以要转换成字符串类型
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        module.addSerializer(Double.class, ToStringSerializer.instance);
        module.addSerializer(Double.TYPE, ToStringSerializer.instance);
        module.addSerializer(Float.class, ToStringSerializer.instance);
        module.addSerializer(Float.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
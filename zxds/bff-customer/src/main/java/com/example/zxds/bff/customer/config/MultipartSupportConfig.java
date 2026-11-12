package com.example.zxds.bff.customer.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Feign 文件上传配置类
 * <p>
 * 作用：让 Feign 能够发送文件上传（multipart/form-data）请求。
 * <p>
 * 背景：Feign 默认只会把参数当成 JSON 来发送，不会处理文件上传。
 * 所以我们需要在这里手动配置一个支持表单编码的 Encoder（编码器）。
 * <p>
 * 简单理解：
 * 1. 从 Spring 容器里拿到所有的消息转换器（HttpMessageConverter）；
 * 2. 用 SpringEncoder 把它们包装起来，让 Feign 能复用 Spring 的编码能力；
 * 3. 再用 SpringFormEncoder 包一层，让它支持 multipart 表单（也就是文件上传）；
 * 4. 把这个 Encoder 注册成 Bean，Feign 就会自动使用它来发送文件了。
 */
@Configuration
public class MultipartSupportConfig {

    @Bean
    public Encoder feignFormEncoder(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new SpringFormEncoder(new SpringEncoder(() -> new HttpMessageConverters(converters.orderedStream().toList())));
    }

}
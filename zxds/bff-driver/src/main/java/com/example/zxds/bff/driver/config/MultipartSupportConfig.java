package com.example.zxds.bff.driver.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration
public class MultipartSupportConfig {

    @Bean
    public Encoder feignFormEncoder(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new SpringFormEncoder(new SpringEncoder(() -> new HttpMessageConverters(converters.orderedStream().toList())));
    }

}
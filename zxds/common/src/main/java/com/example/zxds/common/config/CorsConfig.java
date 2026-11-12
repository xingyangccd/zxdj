package com.example.zxds.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域（CORS）配置类
 * <p>
 * 作用：允许浏览器从其他域名/端口访问本服务的接口。
 * <p>
 * 因为前后端分离时，前端页面和后端接口通常不在同一个域名下，
 * 浏览器出于安全策略会拦截跨域请求，所以需要在这里显式放行。
 * <p>
 * 具体放行规则：
 * 1. 对所有路径（/**）生效；
 * 2. 允许任意来源的域名访问；
 * 3. 允许携带 Cookie 等凭证；
 * 4. 允许 GET、POST、DELETE、PUT、PATCH、OPTIONS 这些请求方法；
 * 5. 允许任意请求头；
 * 6. 预检请求（OPTIONS）的结果缓存 3600 秒，减少重复请求。
 */
//@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
package com.example.zxds.bff.customer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 接口文档配置类
 * <p>
 * 作用：设置接口文档的基本信息（标题、描述、版本）
 */
@OpenAPIDefinition(
        info = @Info(
                title = "bff-customer",
                description = "智行代驾乘客端BFF",
                version = "1.0"
        ),
        security = @SecurityRequirement(name = "token")
)
@SecurityScheme(
        name = "token",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Configuration
public class SpringDocConfig {

}
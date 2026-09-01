package com.knowledge.knowledgeagentplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // ===== 文档基础信息（显示在 Swagger UI 顶部） =====
                .info(new Info()
                        .title(" knowledge-agent-platform项目 API 文档")
                        .version("1.0.0")
                        .description("Spring Boot 4 + springdoc-openapi 3 自动生成")
                        .contact(new Contact()
                                .name("whw")
                                .email("you@example.com")));

                // ===== 全局鉴权方案（配合 Spring Security + JWT） =====
                // 没有接 JWT 的话，这两行可以先注释掉
//                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
//                .components(new Components().addSecuritySchemes("bearer-jwt",
//                        new SecurityScheme()
//                                .name("bearer-jwt")
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")));
    }
}

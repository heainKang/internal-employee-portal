package com.bit.portal.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Internal Employee Portal API")
                        .description("비트컴퓨터 사내 직원 관리 시스템 REST API\n\n" +
                                "**인증:** 로그인 후 발급된 JWT를 [Authorize] 버튼에 입력하세요.\n" +
                                "- 직원(ROLE_USER): /api/me/** 접근 가능\n" +
                                "- 관리자(ROLE_ADMIN): /api/admin/**, /api/me/** 접근 가능")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

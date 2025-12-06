package com.eCommerce.product.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Product Service API",
                description = "API documentation for Product Service (products, categories, purchase, etc.)",
                version = "v1.0",
                contact = @Contact(
                        name = "E-Commerce Team",
                        email = "support@ecommerce.com"
                )
        ),
        servers = {
                @Server(
                        description = "Product Service",
                        url = "/"
                )
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        description = "JWT Bearer token authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    /**
     * Cấu hình OpenAPI chung:
     *  - Thêm thông tin license (optional)
     *  - Gắn Components nếu sau này cần thêm security scheme nâng cao
     */
    @Bean
    public OpenAPI productOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("E-Commerce Product Service API")
                        .description("API documentation for Product Service (products, categories, purchase, etc.)")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }

    /**
     * Group cho tất cả endpoint của product-service.
     *  - pathsToMatch("/api/v1/products/**", "/api/v1/categories/**") tuỳ bạn đang define URL kiểu gì.
     */
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("product-service")
                .pathsToMatch(
                        "/api/v1/products/**",
                        "/api/v1/categories/**"
                )
                .build();
    }
}

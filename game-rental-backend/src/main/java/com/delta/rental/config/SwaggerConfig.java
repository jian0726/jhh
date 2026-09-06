package com.delta.rental.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("三角洲行动游戏账号租赁平台API")
                        .version("1.0.0")
                        .description("游戏账号租赁平台后端接口文档")
                        .contact(new Contact()
                                .name("Delta Rental Team")
                                .email("support@delta-rental.com")));
    }
}

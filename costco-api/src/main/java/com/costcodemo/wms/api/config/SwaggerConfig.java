package com.costcodemo.wms.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * API documentation, published by springdoc at {@code /swagger-ui/index.html}.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI wmsApi() {
        return new OpenAPI().info(new Info()
                .title("Costco WMS (Demo Reference)")
                .description("Digital tier over a simulated IBM i warehouse core. "
                        + "Reference stand-in for a demonstration -- not real Costco code.")
                .version("1.0.0")
                .contact(new Contact().name("Reference Build")));
    }
}

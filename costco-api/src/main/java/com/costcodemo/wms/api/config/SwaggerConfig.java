package com.costcodemo.wms.api.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * API documentation, published by Springfox at {@code /swagger-ui/index.html}.
 *
 * <p>Springfox 3.0.0 is the final release this project ever made. It has no Spring Boot 3
 * support and no Jakarta namespace build, so it is a hard blocker on any framework upgrade
 * rather than an incidental dependency.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket wmsApi() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.costcodemo.wms.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Costco WMS (Demo Reference)",
                "Digital tier over a simulated IBM i warehouse core. "
                        + "Reference stand-in for a demonstration -- not real Costco code.",
                "1.0.0",
                null,
                new Contact("Reference Build", null, null),
                null,
                null,
                Collections.emptyList());
    }
}

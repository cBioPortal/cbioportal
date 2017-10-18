package org.cbioportal.web.config;

import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.config.annotation.PublicApi;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket publicApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(PublicApi.class))
            .build()
            .useDefaultResponseMessages(false)
            .protocols(new HashSet<>(Arrays.asList("http", "https")))
            .apiInfo(apiInfo());
    }

    @Bean
    public Docket internalApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("internal")
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(InternalApi.class))
            .build()
            .useDefaultResponseMessages(false)
            .protocols(new HashSet<>(Arrays.asList("http", "https")))
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
            "cBioPortal web API [Beta]",
            "A web service for supplying JSON formatted data to cBioPortal clients. " +
                "Please note that this API is currently in beta and subject to change.",
            "1.0 (beta)",
            "http://www.cbioportal.org",
            new Contact("cBioPortal", "www.cbioportal.org", "cbioportal@googlegroups.com"),
            "License",
            "https://github.com/cBioPortal/cbioportal/blob/master/LICENSE");
        return apiInfo;
    }
}

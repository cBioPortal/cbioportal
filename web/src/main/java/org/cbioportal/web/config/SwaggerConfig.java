package org.cbioportal.web.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Comparator;
import java.util.stream.Collectors;


@Configuration
@PropertySource("classpath:springfox.properties")
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .addOpenApiMethodFilter(method -> method.getDeclaringClass().isAnnotationPresent(PublicApi.class))
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
            .group("internal")
            .addOpenApiMethodFilter(method -> method.getDeclaringClass().isAnnotationPresent(InternalApi.class))
            .pathsToMatch("/api/**")
            .build();
    }
   
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("cBioPortal web Public API [Alpha]")
                .description("A web service for supplying JSON formatted data to cBioPortal clients. " +
                        "Please note that this API is currently in beta and subject to change.")
                .version("1.0 (beta). Backwards compatibility will be maintained (after 1.0 release)")
                .license(new License().name("License").url("https://github.com/cBioPortal/cbioportal/blob/master/LICENSE"))
                .contact(new Contact().name("cbioportal").url("https://www.cbioportal.org").email("cbioportal@googlegroups.com")))
            .externalDocs(new ExternalDocumentation()
                .description("SpringShop Wiki Documentation")
                .url("https://springshop.wiki.github.org/docs"));
    }
}

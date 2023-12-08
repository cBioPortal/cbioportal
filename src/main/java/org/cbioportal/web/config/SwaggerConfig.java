package org.cbioportal.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;


@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .addOpenApiMethodFilter(method -> method.getDeclaringClass().isAnnotationPresent(PublicApi.class))
            .addOperationCustomizer(customizeOperation())
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
            .group("internal")
            .addOpenApiMethodFilter(method -> method.getDeclaringClass().isAnnotationPresent(InternalApi.class))
            .addOperationCustomizer(customizeOperation())
            .pathsToMatch("/api/**")
            .build();
    }
   
    @Bean
    public OpenAPI springShopOpenAPI(ObjectMapper customObjectMapper) {
        ModelConverters.getInstance().addConverter(new ModelResolver(customObjectMapper));
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
    
    @Bean
    public OperationCustomizer customizeOperation() {
        return (operation, handlerMethod) -> {
            // TODO: Add HTTP Action to EndPoint should remove eventually 
            String httpMethod = extractHttpMethod(handlerMethod);
            String originalOperationId = operation.getOperationId();
            String newOperationId = originalOperationId + "Using" + httpMethod;

            operation.setOperationId(newOperationId);
            return operation;
        };
    }

    private String extractHttpMethod(HandlerMethod handlerMethod) {
        Annotation[] declaredAnnotations = handlerMethod.getMethod().getDeclaredAnnotations();
        for (var annotation : declaredAnnotations) {
          if (annotation instanceof RequestMapping requestMapping) {
              return Stream.of(requestMapping.method()).findFirst().map(RequestMethod::toString).orElse("");
          }
        }
        return "";
    }
    
}

package org.cbioportal.web.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:springfox.properties")
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .addOpenApiMethodFilter(method -> method.isAnnotationPresent(PublicApi.class))
            .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
            .group("internal")
            .addOpenApiMethodFilter(method -> method.isAnnotationPresent(InternalApi.class))
            .build();
    }
    
//    @Bean
//    public Docket publicApi() {
//        Docket d = new Docket(DocumentationType.SWAGGER_2)
//            .select()
//            .apis(RequestHandlerSelectors.withClassAnnotation(PublicApi.class))
//            .build()
//            .useDefaultResponseMessages(false)
//            .apiInfo(publicApiInfo());
//
//        d.tags(
//            new Tag(PublicApiTags.CANCER_TYPES, "", 0),
//            new Tag(PublicApiTags.STUDIES, "", 1),
//            new Tag(PublicApiTags.PATIENTS, "", 2),
//            new Tag(PublicApiTags.SAMPLES, "", 3),
//            new Tag(PublicApiTags.SAMPLE_LISTS, "", 4),
//            new Tag(PublicApiTags.CLINICAL_ATTRIBUTES, "", 5),
//            new Tag(PublicApiTags.CLINICAL_DATA, "", 6),
//            new Tag(PublicApiTags.MOLECULAR_DATA, "", 7),
//            new Tag(PublicApiTags.MOLECULAR_PROFILES, "", 8),
//            new Tag(PublicApiTags.MUTATIONS, "", 9),
//            new Tag(PublicApiTags.DISCRETE_COPY_NUMBER_ALTERATIONS, "", 10),
//            new Tag(PublicApiTags.COPY_NUMBER_SEGMENTS, "", 11),
//            new Tag(PublicApiTags.GENES, "", 12),
//            new Tag(PublicApiTags.GENE_PANELS, "", 13),
//            new Tag(PublicApiTags.GENERIC_ASSAYS, "", 14),
//            new Tag(PublicApiTags.GENERIC_ASSAY_DATA, "", 15),
//            new Tag(PublicApiTags.INFO, "", 16)
//        );

//    @Bean
//    public Docket internalApi() {
//        return new Docket(DocumentationType.SWAGGER_2).groupName("internal")
//            .select()
//            .apis(RequestHandlerSelectors.withClassAnnotation(InternalApi.class))
//            .build()
//            .useDefaultResponseMessages(false)
//            .apiInfo(internalApiInfo());
//    }

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

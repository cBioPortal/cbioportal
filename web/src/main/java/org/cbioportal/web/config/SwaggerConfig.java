package org.cbioportal.web.config;

import java.util.Collections;

import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.config.annotation.PublicApi;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@Configuration
@EnableSwagger2
@PropertySource("classpath:springfox.properties")
public class SwaggerConfig {
    @Bean
    public Docket publicApi() {
        Docket d = new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(PublicApi.class))
            .build()
            .useDefaultResponseMessages(false)
            .apiInfo(apiInfo());

        d.tags(
            new Tag(PublicApiTags.CANCER_TYPES, "", 0),
            new Tag(PublicApiTags.STUDIES, "", 1),
            new Tag(PublicApiTags.PATIENTS, "", 2),
            new Tag(PublicApiTags.SAMPLES, "", 3),
            new Tag(PublicApiTags.SAMPLE_LISTS, "", 4),
            new Tag(PublicApiTags.CLINICAL_ATTRIBUTES, "", 5),
            new Tag(PublicApiTags.CLINICAL_DATA, "", 6),
            new Tag(PublicApiTags.CLINICAL_EVENTS, "", 7),
            new Tag(PublicApiTags.MOLECULAR_DATA, "", 8),
            new Tag(PublicApiTags.MOLECULAR_PROFILES, "", 9),
            new Tag(PublicApiTags.MUTATIONS, "", 10),
            new Tag(PublicApiTags.DISCRETE_COPY_NUMBER_ALTERATIONS, "", 11),
            new Tag(PublicApiTags.COPY_NUMBER_SEGMENTS, "", 12),
            new Tag(PublicApiTags.GENES, "", 13),
            new Tag(PublicApiTags.GENE_PANELS, "", 14),
            new Tag(PublicApiTags.GENERIC_ASSAYS, "", 15),
            new Tag(PublicApiTags.STRUCTURAL_VARIANTS, "", 16),
            new Tag(PublicApiTags.REFERENCE_GENOME_GENES, "", 17),
            new Tag(PublicApiTags.RESOURCE_DEFINITIONS, "", 18),
            new Tag(PublicApiTags.RESOURCE_DATA, "", 19)
        );

        return d;
    }

    @Bean
    public Docket internalApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("internal")
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(InternalApi.class))
            .build()
            .useDefaultResponseMessages(false)
            .apiInfo(apiInfo());
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
            .displayRequestDuration(true)
            .validatorUrl("")
            .build();
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
            "cBioPortal web API [Beta]",
            "A web service for supplying JSON formatted data to cBioPortal clients. " +
                "Please note that this API is currently in beta and subject to change.",
            "1.0 (beta)",
            null,
            new Contact("cBioPortal", "https://www.cbioportal.org", "cbioportal@googlegroups.com"),
            "License",
            "https://github.com/cBioPortal/cbioportal/blob/master/LICENSE", Collections.emptyList());
        return apiInfo;
    }
}

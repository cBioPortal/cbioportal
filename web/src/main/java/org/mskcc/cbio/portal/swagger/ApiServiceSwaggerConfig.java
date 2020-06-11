package org.mskcc.cbio.portal.swagger;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@PropertySource("classpath:springfox.properties")
public class ApiServiceSwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("legacy")
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.cbioportal.weblegacy"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "cBioPortal web API",
                "A web service for supplying JSON formatted data to cBioPortal clients.",
                "1.0 (beta)",
                "www.cbioportal.org",
                new Contact("cBioPortal", "www.cbioportal.org", "cbioportal@googlegroups.com"),
                "License",
                "https://github.com/cBioPortal/cbioportal/blob/master/LICENSE", Collections.emptyList());
        return apiInfo;
    }
}

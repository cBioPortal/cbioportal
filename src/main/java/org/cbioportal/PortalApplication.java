package org.cbioportal;

import org.cbioportal.persistence.summary.SummaryApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@PropertySources({
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:security.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:maven.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:git.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:springdoc.properties")
})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}

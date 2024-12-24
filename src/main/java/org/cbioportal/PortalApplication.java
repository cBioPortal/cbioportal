package org.cbioportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
@EnableTransactionManagement
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}

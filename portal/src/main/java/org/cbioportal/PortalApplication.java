package org.cbioportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:maven.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:git.properties")
    //@PropertySource(ignoreResourceNotFound=true,value="classpath:portal.properties"),
})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}

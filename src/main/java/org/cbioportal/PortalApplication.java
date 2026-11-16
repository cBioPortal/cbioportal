package org.cbioportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, DataMongoAutoConfiguration.class})
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

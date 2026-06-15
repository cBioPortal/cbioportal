package org.cbioportal.infrastructure.requestlog;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Wires up the MongoDB HTTP request logger. Everything here is gated on {@code
 * request-logging.enabled=true}; when the feature is off no Mongo client is created and no filter
 * is registered, so the rest of the application is unaffected. This is needed because the
 * application deliberately excludes Spring Boot's Mongo auto-configuration (see {@code
 * PortalApplication}), so a dedicated, self-contained Mongo connection is provisioned just for
 * request logging.
 */
@Configuration
@EnableConfigurationProperties(RequestLoggingProperties.class)
@ConditionalOnProperty(prefix = "request-logging", name = "enabled", havingValue = "true")
public class RequestLoggingConfig {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingConfig.class);

  @Bean(destroyMethod = "close")
  public MongoClient requestLogMongoClient(RequestLoggingProperties properties) {
    return MongoClients.create(properties.getMongoUri());
  }

  @Bean
  public MongoTemplate requestLogMongoTemplate(
      MongoClient requestLogMongoClient, RequestLoggingProperties properties) {
    return new MongoTemplate(
        new SimpleMongoClientDatabaseFactory(requestLogMongoClient, properties.getDatabase()));
  }

  @Bean
  public RequestLogService requestLogService(
      MongoTemplate requestLogMongoTemplate, RequestLoggingProperties properties) {
    ensureIndexes(requestLogMongoTemplate, properties);
    return new RequestLogService(
        requestLogMongoTemplate,
        properties.getCollection(),
        properties.getWriterThreads(),
        properties.getQueueCapacity());
  }

  @Bean
  public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(
      RequestLogService requestLogService, RequestLoggingProperties properties) {
    FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RequestLoggingFilter(requestLogService, properties));
    registration.addUrlPatterns("/*");
    // Run first so the whole request lifecycle (including the controller's body read) happens
    // inside this filter, guaranteeing the cached body is fully populated when we capture it.
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.setName("requestLoggingFilter");
    LOG.info(
        "MongoDB request logging enabled; capturing {} into {}/{}",
        properties.getPathPatterns(),
        properties.getDatabase(),
        properties.getCollection());
    return registration;
  }

  /**
   * Create the indexes that back endpoint/path search, plus an optional TTL index that bounds how
   * long captured (potentially sensitive) requests are retained. Failures here must not prevent
   * startup (e.g. Mongo temporarily unreachable), so they are logged and swallowed.
   */
  private void ensureIndexes(MongoTemplate template, RequestLoggingProperties properties) {
    String collection = properties.getCollection();
    try {
      template.indexOps(collection).ensureIndex(new Index().on("endpoint", Sort.Direction.ASC));
      template.indexOps(collection).ensureIndex(new Index().on("path", Sort.Direction.ASC));
      if (properties.getTtlDays() > 0) {
        template
            .indexOps(collection)
            .ensureIndex(
                new Index()
                    .on("lastSeen", Sort.Direction.ASC)
                    .expire(Duration.ofDays(properties.getTtlDays())));
      }
    } catch (RuntimeException ex) {
      LOG.warn("Could not create request-logging indexes: {}", ex.getMessage());
    }
  }
}

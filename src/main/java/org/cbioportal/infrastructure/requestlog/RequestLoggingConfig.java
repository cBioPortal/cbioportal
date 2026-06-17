package org.cbioportal.infrastructure.requestlog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Wires up the ClickHouse HTTP request logger. Everything here is gated on {@code
 * request-logging.enabled=true}; when the feature is off no filter is registered, so the rest of
 * the application is unaffected. Captured requests are written through the application's primary
 * datasource (the same ClickHouse the app already queries) into a dedicated table that is expected
 * to already exist.
 */
@Configuration
@EnableConfigurationProperties(RequestLoggingProperties.class)
@ConditionalOnProperty(prefix = "request-logging", name = "enabled", havingValue = "true")
public class RequestLoggingConfig {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingConfig.class);

  @Bean
  public RequestLogService requestLogService(
      DataSource dataSource, RequestLoggingProperties properties) {
    return new RequestLogService(
        dataSource,
        properties.getTable(),
        properties.isAsyncInsert(),
        resolveGitCommit(),
        properties.getWriterThreads(),
        properties.getQueueCapacity());
  }

  /**
   * The full commit the running backend was built from, suffixed with {@code -dirty} when the build
   * had uncommitted changes (so QC can tell apart captures taken against a clean tag versus a local
   * working tree). Read straight from the {@code git.properties} that the git-commit-id Maven
   * plugin writes onto the classpath at build time, rather than the autoconfigured {@code
   * GitProperties} bean (which doesn't expose the plugin's {@code git.commit.id.full} key in this
   * build). Falls back to {@code "unknown"} when the file is absent (e.g. running from an IDE
   * without the plugin).
   */
  private static String resolveGitCommit() {
    Properties props = new Properties();
    try (InputStream in = RequestLoggingConfig.class.getResourceAsStream("/git.properties")) {
      if (in == null) {
        return "unknown";
      }
      props.load(in);
    } catch (IOException ex) {
      LOG.warn("Could not read git.properties for request logging: {}", ex.getMessage());
      return "unknown";
    }
    String commitId =
        props.getProperty("git.commit.id.full", props.getProperty("git.commit.id", ""));
    if (commitId.isEmpty()) {
      return "unknown";
    }
    return Boolean.parseBoolean(props.getProperty("git.dirty", "false"))
        ? commitId + "-dirty"
        : commitId;
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
        "ClickHouse request logging enabled; capturing {} into {}",
        properties.getPathPatterns(),
        properties.getTable());
    return registration;
  }
}

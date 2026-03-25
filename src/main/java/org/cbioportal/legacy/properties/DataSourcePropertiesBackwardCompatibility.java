package org.cbioportal.legacy.properties;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Provides backward compatibility for legacy spring.datasource.clickhouse.* properties. Maps old
 * clickhouse-specific properties to the new standard spring.datasource.* properties.
 *
 * <p>This allows existing configurations using spring.datasource.clickhouse.* to continue working
 * without modification while encouraging migration to the new standard property names.
 *
 * <p>This configuration only activates when legacy clickhouse properties are present and standard
 * properties are not.
 */
@Configuration
public class DataSourcePropertiesBackwardCompatibility {

  private static final Logger log =
      LoggerFactory.getLogger(DataSourcePropertiesBackwardCompatibility.class);

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "spring.datasource.clickhouse", name = "url")
  @ConditionalOnMissingBean(DataSourceProperties.class)
  @ConfigurationProperties("spring.datasource.clickhouse")
  public DataSourceProperties dataSourceProperties() {
    log.warn(
        "Using legacy spring.datasource.clickhouse.* properties. "
            + "Please migrate to spring.datasource.* for consistency with Spring Boot conventions.");
    return new DataSourceProperties();
  }

  @Bean
  @ConditionalOnProperty(prefix = "spring.datasource.clickhouse", name = "url")
  @ConditionalOnMissingBean(DataSource.class)
  public DataSource dataSource(DataSourceProperties dataSourceProperties) {
    return dataSourceProperties.initializeDataSourceBuilder().build();
  }
}

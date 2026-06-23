package org.cbioportal.infrastructure.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the resource-table feature's datasource and MyBatis session factory.
 *
 * <p>The resource mapper uses its own SqlSessionFactory ('resourceSqlSessionFactory') separate from
 * the primary, to avoid XML parsing conflicts.
 *
 * <p>When spring.clickhouse.secondary.url is configured, it uses a dedicated connection pool.
 * Otherwise, it falls back to the primary datasource.
 *
 * <p>Properties (when using secondary):
 *
 * <pre>
 *   spring.clickhouse.secondary.url=jdbc:ch://host:8443/cbioportal_ext?ssl=true
 *   spring.clickhouse.secondary.username=default
 *   spring.clickhouse.secondary.password=secret
 *   spring.clickhouse.secondary.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver
 * </pre>
 */
@Configuration
@MapperScan(
    basePackages = "org.cbioportal.infrastructure.repository.clickhouse.resource",
    sqlSessionFactoryRef = "resourceSqlSessionFactory")
public class SecondaryClickhouseConfig {

  @Autowired private DataSource primaryDataSource;

  @Autowired(required = false)
  private DataSource secondaryClickhouseDataSource;

  @Bean("secondaryClickhouseDataSource")
  @ConditionalOnProperty(prefix = "spring.clickhouse.secondary", name = "url")
  @ConfigurationProperties("spring.clickhouse.secondary")
  public DataSource secondaryClickhouseDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("resourceSqlSessionFactory")
  public SqlSessionFactory resourceSqlSessionFactory(ApplicationContext applicationContext)
      throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    DataSource ds =
        secondaryClickhouseDataSource != null ? secondaryClickhouseDataSource : primaryDataSource;
    sessionFactory.setDataSource(ds);
    sessionFactory.setMapperLocations(
        applicationContext.getResources("classpath:mappers/resource/*.xml"));
    return sessionFactory.getObject();
  }
}

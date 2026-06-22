package org.cbioportal.infrastructure.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Secondary ClickHouse datasource configuration for the resource-table feature.
 *
 * <p>This creates a separate connection pool pointing to an isolated ClickHouse database (e.g.
 * cbioportal_ext) where the resource_data table and resource_data_unified view live.
 *
 * <p>Activated only when spring.clickhouse.secondary.url is configured. The resource-table mapper
 * is wired to this datasource via a dedicated SqlSessionFactory.
 *
 * <p>Properties:
 *
 * <pre>
 *   spring.clickhouse.secondary.url=jdbc:ch://host:8443/cbioportal_ext?ssl=true
 *   spring.clickhouse.secondary.username=default
 *   spring.clickhouse.secondary.password=secret
 *   spring.clickhouse.secondary.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.clickhouse.secondary", name = "url")
@MapperScan(
    basePackages = "org.cbioportal.infrastructure.repository.clickhouse.resource",
    sqlSessionFactoryRef = "secondaryClickhouseSqlSessionFactory")
public class SecondaryClickhouseConfig {

  @Bean("secondaryClickhouseDataSource")
  @ConfigurationProperties("spring.clickhouse.secondary")
  public DataSource secondaryClickhouseDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("secondaryClickhouseSqlSessionFactory")
  public SqlSessionFactory secondaryClickhouseSqlSessionFactory(
      @Qualifier("secondaryClickhouseDataSource") DataSource dataSource,
      ApplicationContext applicationContext)
      throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.setMapperLocations(
        applicationContext.getResources("classpath:mappers/clickhouse/resource/*.xml"));
    return sessionFactory.getObject();
  }
}

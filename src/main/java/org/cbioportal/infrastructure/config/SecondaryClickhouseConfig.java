package org.cbioportal.infrastructure.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration for the resource-table mapper's own SqlSessionFactory.
 *
 * <p>When {@code spring.clickhouse.secondary.url} is set, a dedicated connection pool is created
 * (not exposed as a standalone bean to avoid interfering with primary DataSource resolution).
 * Otherwise, the primary DataSource is injected via Spring's standard resolution.
 *
 * <p>The resource mapper XML lives under {@code classpath:mappers/resource/} — deliberately outside
 * the primary session factory's {@code classpath:mappers/clickhouse/**} glob.
 */
@Configuration
@MapperScan(
    basePackages = "org.cbioportal.infrastructure.repository.clickhouse.resource",
    sqlSessionFactoryRef = "resourceSqlSessionFactory")
public class SecondaryClickhouseConfig {

  @Bean("resourceSqlSessionFactory")
  public SqlSessionFactory resourceSqlSessionFactory(
      DataSource primaryDataSource, Environment env, ApplicationContext applicationContext)
      throws Exception {

    DataSource ds;
    String secondaryUrl = env.getProperty("spring.clickhouse.secondary.url");
    if (secondaryUrl != null && !secondaryUrl.isBlank()) {
      // Build a dedicated DataSource for the secondary ClickHouse database.
      // Not registered as a bean to avoid ambiguity with the primary DataSource.
      ds =
          DataSourceBuilder.create()
              .url(secondaryUrl)
              .username(env.getProperty("spring.clickhouse.secondary.username", ""))
              .password(env.getProperty("spring.clickhouse.secondary.password", ""))
              .driverClassName(
                  env.getProperty(
                      "spring.clickhouse.secondary.driver-class-name",
                      "com.clickhouse.jdbc.ClickHouseDriver"))
              .build();
    } else {
      // Fall back to primary — for dev/test scenarios where only one DB is available.
      ds = primaryDataSource;
    }

    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(ds);
    sessionFactory.setMapperLocations(
        applicationContext.getResources("classpath:mappers/resource/*.xml"));
    return sessionFactory.getObject();
  }
}

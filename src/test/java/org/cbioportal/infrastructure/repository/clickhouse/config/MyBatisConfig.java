package org.cbioportal.infrastructure.repository.clickhouse.config;

import java.io.IOException;
import javax.sql.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@TestConfiguration
@MapperScan(value = "org.cbioportal.infrastructure.repository.clickhouse")
public class MyBatisConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.clickhouse")
  public DataSourceProperties clickhouseDatSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  public DataSource dataSource() {
    return clickhouseDatSourceProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  public SqlSessionFactoryBean sqlColumnarSessionFactory(
      ResourceLoader resourceLoader, DataSource dataSource, ApplicationContext context)
      throws IOException {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.addMapperLocations(
        context.getResources("classpath:mappers/clickhouse/**/*.xml"));
    return sessionFactory;
  }
}

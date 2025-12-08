package org.cbioportal.infrastructure.config;

import java.io.IOException;
import javax.sql.DataSource;
import org.cbioportal.legacy.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("clickhouse")
@MapperScan(
    value = "org.cbioportal.infrastructure.repository.clickhouse",
    sqlSessionFactoryRef = "sqlColumnarSessionFactory")
public class ClickhouseMyBatisConfig {

  @Bean("sqlColumnarSessionFactory")
  public SqlSessionFactoryBean sqlColumnarSessionFactory(
      DataSource dataSource, ApplicationContext applicationContext) throws IOException {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.addMapperLocations(
        applicationContext.getResources("classpath:mappers/clickhouse/**/*.xml"));

    sessionFactory.setTypeHandlers(new SampleTypeTypeHandler());
    return sessionFactory;
  }
}

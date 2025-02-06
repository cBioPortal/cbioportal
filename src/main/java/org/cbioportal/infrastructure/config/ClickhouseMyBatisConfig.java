package org.cbioportal.infrastructure.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ClickhouseMyBatisConfig {
    
    public void addClickhouseMybatisConfig( @Qualifier("sqlColumnarSessionFactory") SqlSessionFactoryBean sqlSessionFactoryBean,
                                            ApplicationContext applicationContext) throws IOException {
        sqlSessionFactoryBean.addMapperLocations(
        applicationContext.getResources("classpath:mappers/clickhouse/**/*.xml"));
    }
}

package org.cbioportal.infrastructure.config;

import org.cbioportal.legacy.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@MapperScan(value= "org.cbioportal.infrastructure.repository",
        sqlSessionFactoryRef = "v2MappersSqlSessionFactory")
public class MyBatisConfig {

    @Bean("v2MappersSqlSessionFactory")
    public SqlSessionFactoryBean sqlColumnarSessionFactory(@Qualifier("v2DataSource") DataSource dataSource, Resource[] v2MapperLocations) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.addMapperLocations(
            v2MapperLocations);

        sessionFactory.setTypeHandlers(new SampleTypeTypeHandler());
        return sessionFactory;
    }

    @Bean("v2MapperLocations")
    @Profile("default")
    public Resource[] mysqlMapperLocations(ApplicationContext applicationContext) throws IOException {
        return applicationContext.getResources("classpath:mappers/mysql/**/*.xml");
    }

    @Bean("v2MapperLocations")
    @Profile("clickhouse")
    public Resource[] clickhouseMapperLocations(ApplicationContext applicationContext) throws IOException {
        return applicationContext.getResources("classpath:mappers/clickhouse/**/*.xml");
    }
}

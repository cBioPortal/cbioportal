package org.cbioportal.persistence.mybatisclickhouse.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.io.IOException;

@TestConfiguration
@MapperScan("org.cbioportal.persistence.mybatisclickhouse")
public class MyBatisConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.clickhouse")
    public DataSourceProperties clickhouseDatSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource() {
        return clickhouseDatSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    public SqlSessionFactoryBean sqlColumnarSessionFactory(ResourceLoader resourceLoader, DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        var studyViewMapperResource = resourceLoader.getResource("classpath:org/cbioportal/persistence/mybatisclickhouse/StudyViewMapper.xml") ;
        var studyViewFilterMapperResource = resourceLoader.getResource("classpath:org/cbioportal/persistence/mybatisclickhouse/StudyViewFilterMapper.xml");
        var alterationFilterMapperResource = resourceLoader.getResource("classpath:org/cbioportal/persistence/mybatisclickhouse/StudyViewAlterationFilterMapper.xml");
        sessionFactory.setMapperLocations(
            studyViewMapperResource,studyViewFilterMapperResource, alterationFilterMapperResource
        );
        return sessionFactory;
    }
    
    



}

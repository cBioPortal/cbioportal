package org.cbioportal.persistence.mybatiscolumnar.config;

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
@MapperScan("org.cbioportal.persistence.mybatiscolumnar")
public class MyBatisConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.columnar")
    public DataSourceProperties columnarDatSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource() {
        return columnarDatSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    public SqlSessionFactoryBean sqlColumnarSessionFactory(ResourceLoader resourceLoader, DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        var studyViewMapperResource = resourceLoader.getResource("classpath:org/cbioportal/persistence/mybatiscolumnar/StudyViewMapper.xml") ;
        var studyViewFilterMapperResource = resourceLoader.getResource("classpath:org/cbioportal/persistence/mybatiscolumnar/StudyViewFilterMapper.xml");
        sessionFactory.setMapperLocations(
            studyViewMapperResource,studyViewFilterMapperResource
        );
        return sessionFactory;
    }
    
    



}

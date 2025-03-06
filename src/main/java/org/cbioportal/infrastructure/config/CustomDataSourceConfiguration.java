package org.cbioportal.infrastructure.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class CustomDataSourceConfiguration {

    @Bean("v2DataSource")
    @Profile("default")
    public DataSource v2MysqlDataSource() {
        return mysqlDataSource();
    }

    @Bean("v2DataSource")
    @Profile("clickhouse")
    public DataSource v2ClickhouseDataSource() {
        return clickhouseDataSource();
    }

    @Bean("mysqlDataSource")
    public DataSource mysqlDataSource() {
        return mysqlDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean("clickhouseDataSource")
    public DataSource clickhouseDataSource() {
        return clickhouseDatSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.clickhouse")
    public DataSourceProperties clickhouseDatSourceProperties() {
        return new DataSourceProperties();
    }
}

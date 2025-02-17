package org.cbioportal.legacy.properties;

import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
//@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
@Profile("clickhouse")
public class CustomDataSourceConfiguration {
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
}

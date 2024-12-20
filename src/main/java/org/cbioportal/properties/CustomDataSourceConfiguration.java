package org.cbioportal.properties;

import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
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

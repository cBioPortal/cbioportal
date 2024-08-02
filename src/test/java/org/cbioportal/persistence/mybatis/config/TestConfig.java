package org.cbioportal.persistence.mybatis.config;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Properties;

@TestConfiguration
@MapperScan("org.cbioportal.persistence.mybatis")
public class TestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("testdb;DATABASE_TO_UPPER=false;MODE=MYSQL;NON_KEYWORDS=END,KEY,VALUE")
            .addScript("cgds-h2.sql")
            .addScript("testSql.sql")
            .build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        // mapper that mediate string to enum conversions
        factoryBean.setTypeHandlers(new SampleTypeTypeHandler());
        // Create a MyBatis Configuration object
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // Set your custom DatabaseIdProvider
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        // Set your database-to-databaseId mapping in the properties
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("H2", "h2");
        // Add more mappings as needed
        databaseIdProvider.setProperties(properties);
        factoryBean.setDatabaseIdProvider(databaseIdProvider);
        return factoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}

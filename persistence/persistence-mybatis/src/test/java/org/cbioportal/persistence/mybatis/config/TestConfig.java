package org.cbioportal.persistence.mybatis.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@MapperScan("org.cbioportal.persistence.mybatis")
@TestConfiguration
public class TestConfig {

    // mappers that mediate string to enum conversions
    @Bean
    public SampleTypeTypeHandler sampleTypeTypeHandler() {
        return new SampleTypeTypeHandler();
    }

    @Bean

    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("testdb;DATABASE_TO_UPPER=false;MODE=MYSQL")
            .addScript("cgds.sql")
            .addScript("data.sql")
            .build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setTypeHandlers(sampleTypeTypeHandler());
        return factoryBean.getObject();
    }

    @Bean
    public OffsetCalculator offsetCalculator() {
        return new OffsetCalculator();
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}

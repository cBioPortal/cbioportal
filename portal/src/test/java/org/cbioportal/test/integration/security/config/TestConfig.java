package org.cbioportal.test.integration.security.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.persistence.mybatis.typehandler.SampleTypeHelper;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@TestConfiguration
public class TestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("testdb;DATABASE_TO_UPPER=true;MODE=MYSQL")
            .addScript("cgds.sql")
            .addScript("data.sql")
            .build();
    }
    
}

package org.cbioportal.persistence.mybatisclickhouse;

import org.junit.BeforeClass;

import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.BindMode;


public abstract class AbstractTestcontainers {
    @BeforeClass
    public static void beforeAll() {
        
    }
    
    @ClassRule
     public static final ClickHouseContainer clickhouseContainer = 
        new ClickHouseContainer("clickhouse/clickhouse-server:24.5")
            .withUsername("cbio_user")
            .withPassword("P@ssword1")
            .withClasspathResourceMapping("clickhouse_cgds.sql", "/docker-entrypoint-initdb.d/a_schema.sql",
                BindMode.READ_ONLY) 
            .withClasspathResourceMapping("clickhouse_data.sql", "/docker-entrypoint-initdb.d/b_schema.sql",
                BindMode.READ_ONLY)
            .withClasspathResourceMapping("clickhouse/clickhouse.sql", "/docker-entrypoint-initdb.d/c_schema.sql",
                                          BindMode.READ_ONLY);

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                "spring.datasource.clickhouse.url=" + clickhouseContainer.getJdbcUrl(),
                "spring.datasource.clickhouse.password=" + clickhouseContainer.getPassword(),
                "spring.datasource.clickhouse.username=" + clickhouseContainer.getUsername(),
                "spring.datasource.clickhouse.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver"
            );
            values.applyTo(configurableApplicationContext);
        }
    }

}


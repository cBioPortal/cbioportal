package org.cbioportal.persistence.mybatiscolumnar;

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
        new ClickHouseContainer("clickhouse/clickhouse-server:22.6")
            .withDatabaseName("cbioportal")
            .withUsername("cbio_user")
            .withPassword("P@ssword1")
            .withClasspathResourceMapping("clickhouse_cgds.sql", "/docker-entrypoint-initdb.d/a_schema.sql",
                BindMode.READ_ONLY) 
            .withClasspathResourceMapping("clickhouse_data.sql", "/docker-entrypoint-initdb.d/b_schema.sql",
                BindMode.READ_ONLY)
            .withClasspathResourceMapping("clickhouse_views.sql", "/docker-entrypoint-initdb.d/c_schema.sql",
                BindMode.READ_ONLY);;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                "spring.datasource.columnar.url=" + clickhouseContainer.getJdbcUrl(),
                "spring.datasource.columnar.password=" + clickhouseContainer.getPassword(),
                "spring.datasource.columnar.username=" + clickhouseContainer.getUsername(),
                "spring.datasource.columnar.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver"
            );
            values.applyTo(configurableApplicationContext);
        }
    }

}


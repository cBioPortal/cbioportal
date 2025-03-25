package org.cbioportal.test.integration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

public abstract class DatabaseInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               MySQLContainer mysqlContainer) {
        TestPropertyValues values = TestPropertyValues.of(
            String.format("spring.datasource.url=%s?useSSL=false&allowPublicKeyRetrieval=true", mysqlContainer.getJdbcUrl()),
            String.format("spring.datasource.username=%s", mysqlContainer.getUsername()),
            String.format("spring.datasource.password=%s", mysqlContainer.getPassword()),
            "spring.datasource.driver-class-name=com.mysql.jdbc.Driver",
            "spring.datasource.clickhouse.url=jdbc:ch://localhost:8443/cbioportal",
            "spring.datasource.clickhouse.username=dummy",
            "spring.datasource.clickhouse.password=dummy",
            "spring.datasource.clickhouse.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver"
        );
        values.applyTo(configurableApplicationContext);
    }
}

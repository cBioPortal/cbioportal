package org.cbioportal.test.integration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

public abstract class MysqlInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               MySQLContainer mysqlContainer) {
        TestPropertyValues values = TestPropertyValues.of(
            String.format("spring.datasource.url=%s?useSSL=false&allowPublicKeyRetrieval=true", mysqlContainer.getJdbcUrl()),
            String.format("spring.datasource.username=%s", mysqlContainer.getUsername()),
            String.format("spring.datasource.password=%s", mysqlContainer.getPassword())
        );
        values.applyTo(configurableApplicationContext);
    }
}

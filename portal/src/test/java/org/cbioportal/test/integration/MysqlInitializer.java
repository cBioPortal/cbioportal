package org.cbioportal.test.integration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class MysqlInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               SharedMysqlContainer mysqlContainer) {
        TestPropertyValues values = TestPropertyValues.of(
            String.format("spring.datasource.url=%s", mysqlContainer.getJdbcUrl()),
            String.format("spring.datasource.username=%s", mysqlContainer.getUsername()),
            String.format("spring.datasource.password=%s", mysqlContainer.getPassword())
        );
        values.applyTo(configurableApplicationContext);
    }
}

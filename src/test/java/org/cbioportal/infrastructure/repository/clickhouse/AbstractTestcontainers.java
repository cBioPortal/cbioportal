package org.cbioportal.infrastructure.repository.clickhouse;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.BindMode;

public abstract class AbstractTestcontainers {
  @BeforeClass
  public static void beforeAll() {}

  @ClassRule
  public static final ClickHouseContainer clickhouseContainer =
      new ClickHouseContainer("clickhouse/clickhouse-server:24.5")
          .withUsername("cbio_user")
          .withPassword("P@ssword1")
          .withClasspathResourceMapping(
              "clickhouse_cgds.sql", "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
          .withClasspathResourceMapping(
              "clickhouse_data.sql", "/docker-entrypoint-initdb.d/b_schema.sql", BindMode.READ_ONLY)
          .withClasspathResourceMapping(
              "clickhouse/clickhouse.sql",
              "/docker-entrypoint-initdb.d/c_schema.sql",
              BindMode.READ_ONLY);

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      if ("true".equalsIgnoreCase(System.getProperty("db.test.use_remote_clickhouse"))) {
        TestPropertyValues values =
            TestPropertyValues.of(
                "spring.datasource.url=" + System.getProperty("db.test.clickhouse.url"),
                "spring.datasource.password=" + System.getProperty("db.test.clickhouse.password"),
                "spring.datasource.username=" + System.getProperty("db.test.clickhouse.username"),
                "spring.datasource.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver");
        values.applyTo(configurableApplicationContext);
      } else {
        clickhouseContainer.start();
        TestPropertyValues values =
            TestPropertyValues.of(
                "spring.datasource.url=" + clickhouseContainer.getJdbcUrl(),
                "spring.datasource.password=" + clickhouseContainer.getPassword(),
                "spring.datasource.username=" + clickhouseContainer.getUsername(),
                "spring.datasource.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver");
        values.applyTo(configurableApplicationContext);
      }
    }
  }
}

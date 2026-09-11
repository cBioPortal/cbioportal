package org.cbioportal;

import org.cbioportal.config.MybatisTestConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.BindMode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("clickhouse")
@Import(MybatisTestConfiguration.class)
public abstract class AbstractClickhouseIntegrationTest {

    public static final String CLICKHOUSE_URL = "jdbc:ch:https://mecgt250i0.us-east-1.aws.clickhouse.cloud:8443/cgds_public_2025_06_24?zeroDateTimeBehavior=convertToNull";
    public static final String CLICKHOUSE_USERNAME = "app_user";
    public static final String CLICKHOUSE_DRIVER = "com.clickhouse.jdbc.ClickHouseDriver";
    public static final String LOCAL_CLICKHOUSE_USERNAME = "cbio_user";
    public static final String LOCAL_CLICKHOUSE_PASSWORD = "P@ssword1";

    public static final String MYSQL_URL = "jdbc:mysql://rfc80db.cbioportal.org:3306/cgds_public_2025_06_24?zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true";
    public static final String MYSQL_USERNAME = "cgds_user";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    private static final ClickHouseContainer CLICKHOUSE_CONTAINER =
        new ClickHouseContainer("clickhouse/clickhouse-server:24.5")
            .withUsername(LOCAL_CLICKHOUSE_USERNAME)
            .withPassword(LOCAL_CLICKHOUSE_PASSWORD)
            .withClasspathResourceMapping(
                "schema.sql", "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
            .withClasspathResourceMapping(
                "clickhouse_data.sql", "/docker-entrypoint-initdb.d/b_schema.sql", BindMode.READ_ONLY)
            .withClasspathResourceMapping(
                "clickhouse/clickhouse.sql",
                "/docker-entrypoint-initdb.d/c_schema.sql",
                BindMode.READ_ONLY);

    @DynamicPropertySource
    static void registerClickHouseProperties(DynamicPropertyRegistry registry) {
        if (hasRemoteClickhouseOverride()) {
            registry.add("spring.datasource.url",
                    () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_URL", CLICKHOUSE_URL));
            registry.add("spring.datasource.username",
                    () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_USERNAME", CLICKHOUSE_USERNAME));
            registry.add("spring.datasource.password",
                    () -> getEnvOrThrow("TEST_DB_CLICKHOUSE_PASSWORD"));
            registry.add("spring.datasource.driver-class-name",
                    () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_DRIVER", CLICKHOUSE_DRIVER));
            return;
        }

        CLICKHOUSE_CONTAINER.start();
        registry.add("spring.datasource.url", CLICKHOUSE_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", CLICKHOUSE_CONTAINER::getUsername);
        registry.add("spring.datasource.password", CLICKHOUSE_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.clickhouse.jdbc.ClickHouseDriver");
    }

    private static boolean hasRemoteClickhouseOverride() {
        String password = System.getenv("TEST_DB_CLICKHOUSE_PASSWORD");
        return password != null && !password.isBlank();
    }

    private static String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}

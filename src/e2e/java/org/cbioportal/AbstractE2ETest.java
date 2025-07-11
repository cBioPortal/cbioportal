package org.cbioportal;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("clickhouse")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractE2ETest {

    public static final String CLICKHOUSE_URL = "jdbc:ch:https://mecgt250i0.us-east-1.aws.clickhouse.cloud:8443/cgds_public_2025_06_24?zeroDateTimeBehavior=convertToNull";
    public static final String CLICKHOUSE_USERNAME = "app_user";
    public static final String CLICKHOUSE_DRIVER = "com.clickhouse.jdbc.ClickHouseDriver";

    public static final String MYSQL_URL = "jdbc:mysql://rfc80db.cbioportal.org:3306/cgds_public_2025_06_24?zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true";
    public static final String MYSQL_USERNAME = "cgds_user";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Authentication
        registry.add("authenticate", () -> "false");
        registry.add("dat.method", () -> "none");

        // MySQL Test DB
        registry.add("spring.datasource.mysql.url",
                () -> getEnvOrDefault("TEST_DB_MYSQL_URL", MYSQL_URL));
        registry.add("spring.datasource.mysql.username",
                () -> getEnvOrDefault("TEST_DB_MYSQL_USERNAME", MYSQL_USERNAME));
        registry.add("spring.datasource.mysql.password", () -> getEnvOrThrow("TEST_DB_MYSQL_PASSWORD"));
        registry.add("spring.datasource.mysql.driver-class-name",
                () -> getEnvOrDefault("TEST_DB_MYSQL_DRIVER", MYSQL_DRIVER));

        // Clickhouse Test DB
        registry.add("spring.datasource.clickhouse.url",
                () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_URL", CLICKHOUSE_URL));
        registry.add("spring.datasource.clickhouse.username",
                () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_USERNAME", CLICKHOUSE_USERNAME));
        registry.add("spring.datasource.clickhouse.password",
                () -> getEnvOrThrow("TEST_DB_CLICKHOUSE_PASSWORD"));
        registry.add("spring.datasource.clickhouse.driver-class-name",
                () -> getEnvOrDefault("TEST_DB_CLICKHOUSE_DRIVER", CLICKHOUSE_DRIVER));
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

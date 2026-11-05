package org.mskcc.cbio.portal.integrationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.dao.ClickHouseAutoIncrement;
import org.mskcc.cbio.portal.dao.ClickHouseBulkDeleter;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class IntegrationTestBase {
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "cgds_test";
    private static final String DB_USER = "cbio_user";
    private static final String DB_PASSWORD = "somepassword";
    private static final int CLICKHOUSE_HTTP_PORT = 8123;
    private static final String CLICKHOUSE_IMAGE = "clickhouse/clickhouse-server:24.3";
    private static final String SKIP_ENV = "CBIOPORTAL_TEST_DB_SKIP";
    private static final String CGDS_SQL_FILE = "clickhouse_cgds.sql";
    private static final String SEED_SQL_FILE = "seed_mini.sql";

    private static ClickHouseContainer container;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestBase::stopContainer));
    }

    @BeforeClass
    public static void setupTestDatabase() {
        startContainer();
    }

    /**
     * All test classes share one JVM (see the integration-test execution in importer/pom.xml), so
     * the importer's static state would otherwise carry over from earlier tests: bulk-load mode left
     * on, pending bulk loads and deletes, and caches filled from a database that has since been
     * rebuilt. Reset all of it before each test.
     */
    @Before
    public void reCache() {
        ClickHouseBulkLoader.reset();
        ClickHouseBulkDeleter.reset();
        DaoGeneOptimized.getInstance().reCache();
        DaoGenePanel.reCache();
        DaoReferenceGenome.reCache();
        DaoCancerStudy.reCacheAll();
    }

    /**
     * Gives the next test a fresh database without starting a new container: drops the test
     * database and rebuilds it from the schema and seed files already copied into the container.
     * Dropping the whole database, rather than truncating tables, also removes the tables the
     * importer creates while it runs (backup tables, bulk-delete scratch tables, the auto-increment
     * sequence table). The container itself is started once and stopped by the shutdown hook.
     */
    @After
    public void resetDatabase() {
        synchronized (LOCK) {
            if (container != null) {
                rebuildDatabase(container);
            }
        }
        ClickHouseAutoIncrement.resetCounters();
    }

    private static void startContainer() {
        synchronized (LOCK) {
            if (container == null && !shouldSkip()) {
                container = startAndInitializeContainer();
            }
        }
    }

    private static void stopContainer() {
        synchronized (LOCK) {
            if (container != null) {
                container.stop();
                container = null;
            }
        }
    }

    private static void rebuildDatabase(ClickHouseContainer clickhouse) {
        executeClickHouseQuery(clickhouse, "DROP DATABASE IF EXISTS " + DB_NAME + " SYNC");
        executeClickHouseQuery(clickhouse, "CREATE DATABASE " + DB_NAME);
        applySqlFile(clickhouse, "/tmp/" + CGDS_SQL_FILE);
        applySqlFile(clickhouse, "/tmp/" + SEED_SQL_FILE);
    }

    private static boolean shouldSkip() {
        String value = System.getenv(SKIP_ENV);
        if (value == null) {
            value = System.getProperty(SKIP_ENV);
        }
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "1".equals(normalized) || "true".equals(normalized);
    }

    private static ClickHouseContainer startAndInitializeContainer() {
        Path cgdsPath = ensureCgdsSql();
        Path seedPath = ensureSeedSql();
        ClickHouseContainer clickhouse = new ClickHouseContainer(DockerImageName.parse(CLICKHOUSE_IMAGE))
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD)
            .withEnv("CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT", "1")
            .withStartupTimeout(Duration.ofMinutes(5));
        clickhouse.start();
        applySchema(clickhouse, cgdsPath);
        applySeed(clickhouse, seedPath);
        setSystemProperties(clickhouse);
        waitForSchema(clickhouse);
        return clickhouse;
    }

    private static void setSystemProperties(ClickHouseContainer clickhouse) {
        String host = clickhouse.getHost();
        String port = Integer.toString(clickhouse.getMappedPort(CLICKHOUSE_HTTP_PORT));
        String jdbcUrl = buildJdbcUrl(host, port, DB_NAME);

        System.setProperty("db.test.host", host);
        System.setProperty("db.test.port", port);
        System.setProperty("db.test.database", DB_NAME);
        System.setProperty("db.test.username", DB_USER);
        System.setProperty("db.test.password", DB_PASSWORD);

        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", DB_USER);
        System.setProperty("spring.datasource.password", DB_PASSWORD);
        System.setProperty("spring.datasource.driver-class-name", "com.clickhouse.jdbc.ClickHouseDriver");

        // Refresh the JDBC datasource to pick up the new container host/port.
        JdbcUtil.setDataSource(buildTestDataSource(jdbcUrl));
    }

    private static TransactionAwareDataSourceProxy buildTestDataSource(String jdbcUrl) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setUrl(jdbcUrl);
        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        dataSource.setMaxTotal(50);
        dataSource.setMaxIdle(5);
        dataSource.setMaxWaitMillis(10000);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    private static void waitForSchema(ClickHouseContainer clickhouse) {
        String jdbcUrl = buildJdbcUrl(
            clickhouse.getHost(),
            Integer.toString(clickhouse.getMappedPort(CLICKHOUSE_HTTP_PORT)),
            DB_NAME);
        long deadline = System.currentTimeMillis() + Duration.ofMinutes(5).toMillis();
        String schemaQuery = String.format("SELECT count(*) FROM %s.info", DB_NAME);
        while (System.currentTimeMillis() < deadline) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(schemaQuery)) {
                if (result.next()) {
                    if (result.getInt(1) < 1) {
                        continue;
                    }
                    return;
                }
            } catch (Exception ex) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for test database", interrupted);
                }
            }
        }
        throw new IllegalStateException("Timed out waiting for test database schema initialization");
    }

    private static String buildJdbcUrl(String host, String port, String database) {
        return String.format(
            "jdbc:clickhouse://%s:%s/%s",
            host,
            port,
            database);
    }

    private static void applySchema(ClickHouseContainer clickhouse, Path schemaPath) {
        String containerSchemaPath = "/tmp/" + CGDS_SQL_FILE;
        clickhouse.copyFileToContainer(MountableFile.forHostPath(schemaPath), containerSchemaPath);

        executeClickHouseQuery(clickhouse, "CREATE DATABASE IF NOT EXISTS " + DB_NAME);

        applySqlFile(clickhouse, containerSchemaPath);
    }

    private static void applySeed(ClickHouseContainer clickhouse, Path seedPath) {
        String containerSeedPath = "/tmp/" + SEED_SQL_FILE;
        clickhouse.copyFileToContainer(MountableFile.forHostPath(seedPath), containerSeedPath);

        applySqlFile(clickhouse, containerSeedPath);
    }

    private static void applySqlFile(ClickHouseContainer clickhouse, String containerSeedPath) {
        List<String> command = baseClickHouseCommand();
        command.add("--database");
        command.add(DB_NAME);
        command.add("--multiquery");
        command.add("--queries-file");
        command.add(containerSeedPath);
        executeClickHouseCommand(clickhouse, command);
    }

    private static void executeClickHouseQuery(ClickHouseContainer clickhouse, String query) {
        List<String> command = baseClickHouseCommand();
        command.add("--query");
        command.add(query);
        executeClickHouseCommand(clickhouse, command);
    }

    private static List<String> baseClickHouseCommand() {
        List<String> command = new ArrayList<>();
        command.add("clickhouse-client");
        command.add("--user");
        command.add(DB_USER);
        command.add("--password");
        command.add(DB_PASSWORD);
        return command;
    }

    private static void executeClickHouseCommand(ClickHouseContainer clickhouse, List<String> command) {
        try {
            Container.ExecResult result = clickhouse.execInContainer(command.toArray(new String[0]));
            if (result.getExitCode() != 0) {
                String error = result.getStderr();
                if (error == null || error.isBlank()) {
                    error = result.getStdout();
                }
                throw new IllegalStateException("ClickHouse command failed: " + error);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute ClickHouse command", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while executing ClickHouse command", ex);
        }
    }

    // The importer's own copy of the schema, read from this repository. It is not the same file as
    // the web app's src/main/resources/db-scripts/clickhouse/init/schema.sql (see the RFC's
    // "Choose the schema's source of truth").
    private static Path ensureCgdsSql() {
        Path cgdsPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", CGDS_SQL_FILE);
        if (!Files.exists(cgdsPath)) {
            throw new IllegalStateException(CGDS_SQL_FILE + " not found at " + cgdsPath);
        }
        return cgdsPath;
    }

    private static Path ensureSeedSql() {
        Path seedPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", SEED_SQL_FILE);
        if (!Files.exists(seedPath)) {
            throw new IllegalStateException("seed_mini.sql not found at " + seedPath);
        }
        return seedPath;
    }
}

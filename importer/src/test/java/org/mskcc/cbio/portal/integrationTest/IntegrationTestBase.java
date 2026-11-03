package org.mskcc.cbio.portal.integrationTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.dao.ClickHouseAutoIncrement;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
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
    private static final String TARGET_DIR = "target/test-db";
    private static final String VERSION_FILE = "cbioportal.version";
    private static final String CGDS_SQL_FILE = "clickhouse_cgds.sql";
    private static final String SEED_SQL_FILE = "seed_mini.sql";
    private static final String CGDS_URL_TEMPLATE =
        "https://raw.githubusercontent.com/cBioPortal/cbioportal/%s/src/test/resources/clickhouse_cgds.sql";
    private static final Pattern CBIOPORTAL_VERSION_PATTERN =
        Pattern.compile("<cbioportal\\.version>\\s*([^<]+)\\s*</cbioportal\\.version>");

    private static ClickHouseContainer container;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestBase::stopContainer));
    }

    @BeforeClass
    public static void setupTestDatabase() {
        startContainer();
    }

    @Before
    public void reCache() {
        DaoCancerStudy.reCacheAll();
    }

    @After
    public void resetDatabase() {
        recreateContainer();
        ClickHouseAutoIncrement.resetCounters();
    }

    @AfterClass
    public static void teardownTestDatabase() {
        stopContainer();
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

    private static void recreateContainer() {
        stopContainer();
        startContainer();
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

    private static Path ensureCgdsSql() {
        //FIXME Uncomment when cbioportal contains the complete clickhouse_cgds.sql for the current version
        /*
        String cbioportalVersion = readCbioportalVersion();
        Path targetDir = Paths.get(System.getProperty("user.dir"), TARGET_DIR);
        Path cgdsPath = targetDir.resolve(CGDS_SQL_FILE);
        Path versionPath = targetDir.resolve(VERSION_FILE);
        try {
            Files.createDirectories(targetDir);
            if (!Files.exists(cgdsPath) || !Files.exists(versionPath)
                || !cbioportalVersion.equals(readFile(versionPath).trim())) {
                downloadCgdsSql(cbioportalVersion, cgdsPath);
                Files.writeString(versionPath, cbioportalVersion, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to prepare clickhouse_cgds.sql for integration tests", ex);
        }
        */
        Path cgdsPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", CGDS_SQL_FILE);
        return cgdsPath;
    }

    private static Path ensureSeedSql() {
        Path seedPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", SEED_SQL_FILE);
        if (!Files.exists(seedPath)) {
            throw new IllegalStateException("seed_mini.sql not found at " + seedPath);
        }
        return seedPath;
    }

    private static String readCbioportalVersion() {
        Path pomPath = Paths.get(System.getProperty("user.dir"), "pom.xml");
        try {
            String pomContents = readFile(pomPath);
            Matcher matcher = CBIOPORTAL_VERSION_PATTERN.matcher(pomContents);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read pom.xml for cbioportal.version", ex);
        }
        throw new IllegalStateException("cbioportal.version not found in pom.xml");
    }

    private static String readFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static void downloadCgdsSql(String version, Path destination) throws IOException {
        String url = String.format(CGDS_URL_TEMPLATE, version);
        System.out.println("Downloading clickhouse_cgds.sql for " + version + "...");
        try (InputStream input = new URL(url).openStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

package org.cbioportal.test.integration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MySQLContainer;

public class SharedMysqlContainer extends MySQLContainer<SharedMysqlContainer> {

    private static final String IMAGE_VERSION = "mysql:5.7";
    private static SharedMysqlContainer container;

    private SharedMysqlContainer() {
        super(IMAGE_VERSION);
    }

    public static SharedMysqlContainer getInstance() {
        if (container == null) {
            container = new SharedMysqlContainer()
                .withFileSystemBind(absolutePath("db-scripts/src/main/resources/cgds.sql").toString(), "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
                .withFileSystemBind(absolutePath("portal/src/test/resources/seed_mini.sql").toString(), "/docker-entrypoint-initdb.d/b_seed.sql", BindMode.READ_ONLY)
//                .withFileSystemBind("/tmp/cbio_ingegration_test_data/", "/var/lib/mysql/")
                .withStartupTimeout(Duration.ofMinutes(10));

        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }

    /**
     * @param path path to file or directory from the root of the maven project
     * @return Absolute path to file
     */
    private static Path absolutePath(String path) {
        return Paths.get(new File("../" + path).getAbsolutePath());
    }
}

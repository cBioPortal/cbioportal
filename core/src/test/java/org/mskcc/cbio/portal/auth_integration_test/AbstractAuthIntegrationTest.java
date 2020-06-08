package org.mskcc.cbio.portal.auth_integration_test;

import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@TestPropertySource(properties = "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver")
public abstract class AbstractAuthIntegrationTest {

    private static final String dbDockerImage = "mysql:5.7";
    private static final String dbName = "cgds_test";
    private static final String dbUser = "cbio_user";
    private static final String dbPassword = "somepassword";
    private static final String dbHost = "cbioDB";

    private static final String cbioImageName = "cbio-security-integration-test";

    public static final Network network = Network.newNetwork();

    public static MariaDBContainer mariadb = (MariaDBContainer) new MariaDBContainer(dbDockerImage)
            .withDatabaseName(dbName)
            .withUsername(dbUser)
            .withPassword(dbPassword)
            .withNetworkAliases(dbHost)
            .withNetwork(network)
            .withFileSystemBind(absolutePath("db-scripts/src/main/resources/cgds.sql").toString(), "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
            .withFileSystemBind("/tmp/cbio_ingegration_test_data/", "/var/lib/mysql/")
            .withStartupTimeout(Duration.ofMinutes(10));

        public static final ImageFromDockerfile cbioImage = new ImageFromDockerfile(cbioImageName, false)
            .withDockerfile(absolutePath("Dockerfile"));
    // FIXME needed await correct implementation of withDockerfile (https://github.com/testcontainers/testcontainers-java/issues/2771)
    //            .withDockerfile(absolutePath("docker/web-and-data/Dockerfile"));

    public static GenericContainer cbioportal = new GenericContainer(cbioImage)
            .withReuse(true) // experimental: container reuse together with .testcontainers.properties?
            .withNetwork(network)
            .withCommand("sh","-c", String.format(
                    "java -Xms2G -Xmx8G" +
                    " -Dauthenticate=${AUTHENTICATE}" +
                    " -Ddb.user=" + dbUser +
                    " -Ddb.password=" + dbPassword +
                    " -Ddb.host=" + dbHost +
                    " -Ddb.portal_db_name=" + dbName +
                    " -Ddb.driver=com.mysql.jdbc.Driver " +
                    " -Ddb.connection_string=jdbc:mysql://" + dbHost + "/" +
                    " -jar /webapp-runner.jar /cbioportal-webapp"
            ))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/api/health"))
            .withStartupTimeout(Duration.ofMinutes(10));

    public static BrowserWebDriverContainer chrome =
            new BrowserWebDriverContainer()
                    .withCapabilities(new ChromeOptions());

    static {
        try {
            mariadb.start();
            chrome.start();

            // FIXME needed await correct implementation of withDockerfile (https://github.com/testcontainers/testcontainers-java/issues/2771)
            if (! Files.exists(absolutePath("Dockerfile")))
                Files.copy(absolutePath("docker/web-and-data/Dockerfile"), absolutePath("Dockerfile"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path path to file or directory from the root of the maven project
     * @return Absolute path to file
     */
    private static Path absolutePath(String path) {
        return Paths.get(new File("../" + path).getAbsolutePath());
    }

}

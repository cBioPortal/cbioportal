package org.cbioportal.test.integration.security;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.cbioportal.test.integration.OAuth2ResourceServerKeycloakInitializer;
import org.cbioportal.test.integration.MysqlInitializer;
import org.cbioportal.test.integration.OAuth2KeycloakInitializer;
import org.cbioportal.test.integration.SamlKeycloakInitializer;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.cbioportal.test.integration.security.util.Util.isHostMappingPresent;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

public class AbstractContainerTest {

    public final static int CBIO_PORT = 8080;
    public final static int SESSION_SERVICE_PORT = 5000;
    public final static int MONGO_PORT = 27017;
    public final static int MYSQL_PORT = 3306;
    public final static int MOCKSERVER_PORT = 8085;
    public final static String DOWNLOAD_FOLDER = "/tmp/browser_downloads";
    
    private static final String SESSION_IMAGE_VERSION = "docker.io/cbioportal/session-service:0.6.1";
    private static final String MONGO_IMAGE_VERSION = "docker.io/mongo:3.7.9";
    private static final String KEYCLOAK_IMAGE_VERSION = "quay.io/keycloak/keycloak:23.0";
    private static final String MYSQL_IMAGE_VERSION = "mysql:5.7";
    private static final String MOCKSERVER_IMAGE_VERSION = "docker.io/mockserver/mockserver:5.15.0";

    static final GenericContainer sessionServiceContainer;
    static final GenericContainer mongoContainer;
    static final MySQLContainer mysqlContainer;
    static final GenericContainer mockServerContainer;
    static final KeycloakContainer keycloakContainer;
    static final BrowserWebDriverContainer chromedriverContainer;

    static {

        String hostToCheck = "host.testcontainers.internal";
        String ipAddressToCheck = "127.0.0.1";
        try {
            if (!isHostMappingPresent(hostToCheck, ipAddressToCheck)) {
                throw new IllegalStateException(hostToCheck + " is not mapped to " + ipAddressToCheck + " in /etc/hosts. Please add this mapping.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read /etc/hosts file.", e);
        }

        sessionServiceContainer = new GenericContainer(DockerImageName.parse(SESSION_IMAGE_VERSION))
            .withAccessToHost(true)
            .withEnv("SERVER_PORT", "5000")
            .withEnv("JAVA_OPTS", "-Dspring.data.mongodb.uri=mongodb://host.testcontainers.internal:27017/session-service");
        sessionServiceContainer.setPortBindings(ImmutableList.of(String.format("%s:5000", SESSION_SERVICE_PORT)));

        mongoContainer = new GenericContainer(DockerImageName.parse(MONGO_IMAGE_VERSION))
            .withEnv("MONGO_INITDB_DATABASE", "session_service");
        mongoContainer.setPortBindings(ImmutableList.of(String.format("%s:27017", MONGO_PORT, MONGO_PORT)));

        keycloakContainer = new KeycloakContainer(KEYCLOAK_IMAGE_VERSION)
            .withRealmImportFile("security/keycloak-configuration-generated.json")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .withEnv("KC_HOSTNAME", "host.testcontainers.internal")
            .withEnv("KC_HOSTNAME_ADMIN", "localhost");

        mockServerContainer = new GenericContainer(MOCKSERVER_IMAGE_VERSION)
            .withExposedPorts(1080);
        mockServerContainer.setPortBindings(ImmutableList.of(String.format("%s:1080", MOCKSERVER_PORT)));

        mysqlContainer = (MySQLContainer) new MySQLContainer(MYSQL_IMAGE_VERSION)
            .withClasspathResourceMapping("cgds.sql", "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
            .withClasspathResourceMapping("seed_mini.sql", "/docker-entrypoint-initdb.d/b_seed.sql", BindMode.READ_ONLY)
            .withStartupTimeout(Duration.ofMinutes(10));

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_FOLDER);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.prompt_for_download", "false");
        prefs.put("download.directory_upgrade", "true");
        options.setExperimentalOption("prefs", prefs);

        chromedriverContainer = new BrowserWebDriverContainer<>()
            .withCapabilities(options)
            // activate this to record movies of the tests (great for debugging)
            .withRecordingMode(RECORD_ALL, new File("/home/pnp300/"))
            .withAccessToHost(true);

        mysqlContainer.start();
        sessionServiceContainer.start();
        mongoContainer.start();
        mockServerContainer.start();
        keycloakContainer.start();
        chromedriverContainer.start();
    }

    // Update application properties with connection info on Keycloak container
    public static class MySamlKeycloakInitializer extends SamlKeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    }

    // Update application properties with connection info on Keycloak container
    public static class MyOAuth2KeycloakInitializer extends OAuth2KeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    }

    // Update application properties with connection info on Mysql container
    public static class MyMysqlInitializer extends MysqlInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, mysqlContainer);
        }
    }

    public static class MyOAuth2ResourceServerKeycloakInitializer extends OAuth2ResourceServerKeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    } 

    // Expose the ports for the cBioPortal Spring application and keycloak inside 
    // the Chrome container. Each address is available on http://host.testcontainers.internal:<port>
    // in the browser container.
    public static class PortInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                "server.port=" + CBIO_PORT
            );
            values.applyTo(applicationContext);
            applicationContext.addApplicationListener(
                (ApplicationListener<WebServerInitializedEvent>) event -> {
                    Testcontainers.exposeHostPorts(CBIO_PORT, keycloakContainer.getHttpPort(), MONGO_PORT);
                    keycloakContainer.setPortBindings(ImmutableList.of(String.format("%s:8080", keycloakContainer.getHttpPort())));
                });
        }
    }

}
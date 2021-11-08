package org.cbioportal.test.integration.security;

import static org.cbioportal.test.integration.security.SamlAuthIntegrationTest.MyKeycloakInitializer;
import static org.cbioportal.test.integration.security.SamlAuthIntegrationTest.MyMysqlInitializer;
import static org.cbioportal.test.integration.security.SamlAuthIntegrationTest.PortInitializer;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.cbioportal.PortalApplication;
import org.cbioportal.test.integration.KeycloakInitializer;
import org.cbioportal.test.integration.MysqlInitializer;
import org.cbioportal.test.integration.SharedChromeContainer;
import org.cbioportal.test.integration.SharedKeycloakContainer;
import org.cbioportal.test.integration.SharedMysqlContainer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {PortalApplication.class}
)
@TestPropertySource(
    properties = {
        "app.name=cbioportal",
        "server.port=8080",
        "filter_groups_by_appname=true",
        "authenticate=saml",
        "dat.method=oauth2",
        // DB settings (also see MysqlInitializer)
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // SAML settings
        "saml.keystore.location=classpath:/security/samlKeystore.jks",
        "saml.keystore.password=P@ssword1",
        "saml.keystore.private-key.key=secure-key",
        "saml.keystore.private-key.password=P@ssword1",
        "saml.keystore.default-key=secure-key",
        "saml.idp.comm.binding.settings=defaultBinding",
        "saml.sp.metadata.entitybaseurl=#{null}",
        "saml.sp.metadata.entityid=cbioportal",
        "saml.sp.metadata.wantassertionsigned=true",
        "saml.idp.metadata.attribute.email=email",
        "saml.idp.metadata.attribute.role=Role",
        "saml.logout.local=false",
        // Keycloak host settings (also see KeycloakInitializer)
        "dat.oauth2.clientId=cbioportal_api",
        "dat.oauth2.clientSecret=client_secret",
        // Redirect URL to cBiopPortal application from perspective of browser
        "dat.oauth2.redirectUri=http://host.testcontainers.internal:8080/api/data-access-token/oauth2",
        "dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles"
    }
)
@ContextConfiguration(initializers = {
    MyMysqlInitializer.class,
    MyKeycloakInitializer.class,
    PortInitializer.class
})
public class SamlAuthIntegrationTest {

    private final static int CBIO_PORT = 8080;
    public final static String CBIO_URL_FROM_BROWSER =
        String.format("http://host.testcontainers.internal:%d", CBIO_PORT);

    @ClassRule
    public static SharedMysqlContainer mysqlContainer = SharedMysqlContainer.getInstance();

    @ClassRule
    public static KeycloakContainer keycloakContainer = SharedKeycloakContainer.getInstance();

    @ClassRule
    public static BrowserWebDriverContainer chrome = SharedChromeContainer.getInstance();

    // Update application properties with connection info on Keycloak container
    public static class MyKeycloakInitializer extends KeycloakInitializer {
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

    // Expose the ports for the cBioPortal Spring application and keycloak inside 
    // the Chrome container. Each address is available on http://host.testcontainers.internal:<port>
    // in the browser container.
    public static class PortInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(
                (ApplicationListener<WebServerInitializedEvent>) event -> {
                    Testcontainers.exposeHostPorts(CBIO_PORT, keycloakContainer.getHttpPort());
                });
        }
    }

    @Test
    public void loginSuccess() {
        RemoteWebDriver driver = performLogin();
        WebElement loggedInButton = driver.findElement(By.id("dat-dropdown"));
        Assertions.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
    }

    @Test
    public void downloadOfflineToken() throws Exception {
        RemoteWebDriver driver = performLogin();
        driver.findElement(By.id("dat-dropdown")).click();
        driver.findElement(By.linkText("Data Access Token")).click();
        driver.findElement(By.xpath("//button[text()='Download Token']")).click();

        await().atMost(Duration.ofSeconds(2)).until(downloadedFile());

        Assertions.assertTrue(downloadedFile().call());
    }

    private RemoteWebDriver performLogin() {
        RemoteWebDriver driver = chrome.getWebDriver();
        driver.get(CBIO_URL_FROM_BROWSER);
        try {
            // when the cbioportal logo is visible, skip login
            driver.findElement(By.id("cbioportal-logo"));
        } catch (NoSuchElementException e) {
            WebElement userNameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("kc-login"));
            userNameInput.sendKeys("testuser");
            passwordInput.sendKeys("P@ssword1");
            loginButton.click();
        }
        return driver;
    }

    private boolean containerFileExists(
        @Nonnull final GenericContainer container, @Nonnull String path)
        throws IOException, InterruptedException {
        Assert.notNull(container, "Containers is null");
        Assert.isTrue(!path.isEmpty(), "Path string is empty");
        Container.ExecResult r = container.execInContainer("/bin/sh", "-c",
            "if [ -f " + path
                + " ] ; then echo '0' ; else (>&2 echo '1') ; fi");
        return !r.getStderr().contains("1");
    }

    private Callable<Boolean> downloadedFile() {
        return () -> containerFileExists(chrome,
            String.format("%s/cbioportal_data_access_token.txt",
                SharedChromeContainer.DOWNLOAD_FOLDER));
    }

}
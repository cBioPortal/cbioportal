package org.mskcc.cbio.portal.auth_integration_test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
public class TestAuthIntegrationSaml extends AbstractAuthIntegrationTest {

    private static String kcDockerImage = "jboss/keycloak:8.0.2";
    private static String kcAdminName = "admin";
    private static String kcAdminPassword = "admin";

    protected static String cbioUrl = "http://cbio:8080/";

    public static final GenericContainer keycloak =
            new GenericContainer(kcDockerImage)
                    .withEnv("KEYCLOAK_USER", kcAdminName)
                    .withEnv("KEYCLOAK_PASSWORD", kcAdminPassword)
                    .withEnv("KEYCLOAK_IMPORT", "/tmp/realm.json")
                    .withFileSystemBind(
                            absolutePath("core/src/test/resources/auth_integration/keycloak/keycloak-configuration-generated.json").toString()
                            , "/tmp/realm.json",
                            BindMode.READ_ONLY
                    )
                    .withNetwork(network)
                    .withNetworkAliases("cbiokc")
                    .waitingFor(Wait.forLogMessage(".*Keycloak.*started in.*", 1));


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {

        // start keycloak and export saml idp metadata
        keycloak.start();
        String samlIdpMetadata = keycloak.execInContainer("curl", "http://cbiokc:8080/auth/realms/cbio/protocol/saml/descriptor").getStdout();

        System.out.println("portal starting with saml ...");
        cbioportal.withFileSystemBind(
                absolutePath("core/src/test/resources/auth_integration/portal.properties.saml").toString()
                , "/cbioportal-webapp/WEB-INF/classes/portal.properties",
                BindMode.READ_ONLY
        );
        cbioportal.withFileSystemBind(
                tempFile(samlIdpMetadata),
                "/cbioportal-webapp/WEB-INF/classes/client-tailored-saml-idp-metadata.xml",
                BindMode.READ_ONLY
        );
        cbioportal.withFileSystemBind(
                absolutePath("core/src/test/resources/auth_integration/keycloak/samlKeystore.jks").toString(),
                "/cbioportal-webapp/WEB-INF/classes/samlKeystore.jks",
                BindMode.READ_ONLY
        );
        cbioportal.withEnv("AUTHENTICATE", "saml");
        cbioportal.start();
        System.out.println("portal ready (port is: " + cbioportal.getMappedPort(8080) +")");
    }

    @AfterClass
    public static void tearDown() {
        cbioportal.stop();
        keycloak.stop();
    }

    private static String tempFile(String samlIdpMetadata) throws IOException {
        String absolutePath = File.createTempFile("temp-idp-metadata", Long.toString(System.nanoTime())).getAbsolutePath();
        BufferedWriter bw = new BufferedWriter(new FileWriter(absolutePath));
        bw.write(samlIdpMetadata);
        bw.close();
        return absolutePath;
    }

    @Test
    public void myFirstTest() {
        RemoteWebDriver driver = chrome.getWebDriver();
        driver.get(cbioUrl);
        WebElement userNameInput = driver.findElement(By.id("username"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("kc-login"));
        userNameInput.sendKeys("testuser");
        passwordInput.sendKeys("P@ssword1");
        loginButton.click();
        WebElement loggedInButton = driver.findElement(By.id("dat-dropdown"));
        Assert.assertEquals("Logged in as testuser@thehyve.nl", loggedInButton.getText());
    }

}

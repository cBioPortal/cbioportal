package org.mskcc.cbio.portal.auth_integration_test;

import org.hamcrest.Matchers;
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
    protected static String kcUrl = "http://cbiokc:8080";
    private static String kcIdpMetadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><EntitiesDescriptor Name=\"urn:keycloak\" xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" xmlns:dsig=\"http://www.w3.org/2000/09/xmldsig#\"> <EntityDescriptor entityID=\"cbioportal\"> <IDPSSODescriptor WantAuthnRequestsSigned=\"true\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\"> <KeyDescriptor use=\"signing\"> <dsig:KeyInfo> <dsig:KeyName>LM8WSjnaCOUqXS7LVrGAp1dn7Nvkh1_Fj8x_1div-1E</dsig:KeyName> <dsig:X509Data> <dsig:X509Certificate>MIIClzCCAX8CBgFyfjGqjTANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDARjYmlvMB4XDTIwMDYwNDA3MTYyM1oXDTMwMDYwNDA3MTgwM1owDzENMAsGA1UEAwwEY2JpbzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK70DgilyAXyyqsH/PpbiQpQs4hrL0YKfM4JD9U+trItX+CUu7ATLJQ18yq+zqcONH+uDgK5jDV5rqwL7Tp8qHpaWyGwAOXi9z4bSEiF6E0475TMpGImHfNidcRxx/BliP40jWyY8lMJcB0khmM1EppmSky51BOxrCR0cFlxnuE+gF22tqAFkQNeM+b7udH/tkDOG5yMX20kAxqiKbg+l1LIc6sIGYkZ+MkkiaW3DddwxsS/r4ZCfNAyVS5xr4ok1Aq86pkubpbpByUrOBrbjuSkXBMX10P+DzoNcolwwIpeC8BSj10PQ+U0q6AvcQ5Z/Qz7x8qsNQKc5EXoUJOSO8ECAwEAATANBgkqhkiG9w0BAQsFAAOCAQEASM3edg85FYxEB5FyilJ6mx3cP6wXn8DmdbhvogVkr6W8tQCwmVpFSoVBDtGIWqdFVrP2wscTgbbAYIG91sWrIpPR0F+CW1iYeqDKUylnVwkZLDPSnDygN0TtzMzazFTE7/p15maaR95m5nJn9sNhFfwWCQg2ezOQoEJIvwKtSEpZGbhZ7978UB+4i9lCu2Y9dnWfnLUJe42opjSs84dcTvvpZGAVDHNc1tOiIgnz9EBW7bwJ09qbuUY6sfb1QmQd1GMq1ghe2xZHxucNzuXAS3on0z+8ZmuqFxaoYAAzPXOO0jAm+DKiJrXsnXa/rbLEnR+2fHWzTvDh7fXlJPRfgQ==</dsig:X509Certificate> </dsig:X509Data> </dsig:KeyInfo> </KeyDescriptor> <SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + kcUrl + "/auth/realms/cbio/protocol/saml\" /> <SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"" + kcUrl + "/auth/realms/cbio/protocol/saml\" /> <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</NameIDFormat> <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat> <NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</NameIDFormat> <NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</NameIDFormat> <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + kcUrl + "/auth/realms/cbio/protocol/saml\" /> <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"" + kcUrl + "/auth/realms/cbio/protocol/saml\" /> <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"" + kcUrl + "/auth/realms/cbio/protocol/saml\" /> </IDPSSODescriptor> </EntityDescriptor> </EntitiesDescriptor>\n";

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
                    .withNetworkAliases("cbiokc");


    @BeforeClass
    public static void setUp() throws IOException {
        keycloak.start();
        System.out.println("portal starting with saml ...");
        cbioportal.withFileSystemBind(
                absolutePath("core/src/test/resources/auth_integration/portal.properties.saml").toString()
                , "/cbioportal-webapp/WEB-INF/classes/portal.properties",
                BindMode.READ_ONLY
        );
        cbioportal.withFileSystemBind(
                tempIdpMetadataFile(),
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

    private static String tempIdpMetadataFile() throws IOException {
        String absolutePath = File.createTempFile("temp-idp-metadata", Long.toString(System.nanoTime())).getAbsolutePath();
        BufferedWriter bw = new BufferedWriter(new FileWriter(absolutePath));
        bw.write(kcIdpMetadata);
        bw.close();
        return absolutePath;
    }

    @AfterClass
    public static void tearDown() {
        cbioportal.stop();
        keycloak.stop();
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
        // TODO We are getting now "You are not authorized to access this resource..." page.
        // Assert the username when the authorization problem will be solved.
        Assert.assertThat(driver.getTitle(), Matchers.startsWith("cBioPortal for Cancer Genomics"));
    }

}

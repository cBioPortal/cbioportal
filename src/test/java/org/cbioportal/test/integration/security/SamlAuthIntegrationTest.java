package org.cbioportal.test.integration.security;

import org.cbioportal.PortalApplication;
import org.cbioportal.test.integration.security.util.Util;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.cbioportal.test.integration.security.ContainerConfig.MyMysqlInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.MySamlKeycloakInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.PortInitializer;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {PortalApplication.class}
)
@TestPropertySource(
    properties = {
        "authenticate=saml",
        //"dat.method=oauth2",
        // DB settings (also see MysqlInitializer)
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // SAML settings
        "spring.security.saml2.relyingparty.registration.keycloak.entity-id=cbioportal",
        "spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].certificate-location=classpath:security/signing-cert.pem",
        "spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].private-key-location=classpath:security/signing-key.pem",
        "saml.idp.metadata.attribute.email=email",
        "saml.idp.metadata.attribute.role=Role",
        // Keycloak host settings (also see KeycloakInitializer)
        //"dat.oauth2.clientId=cbioportal_oauth2",
        //"dat.oauth2.clientSecret=client_secret",
        // Redirect URL to cBiopPortal application from perspective of browser
        //"dat.oauth2.redirectUri=http://host.testcontainers.internal:8080/api/data-access-token/oauth2",
        //"dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles",
        "session.service.url=http://localhost:5000/api/sessions/my_portal/",
        "filter_groups_by_appname=false"
    }
)
@ContextConfiguration(initializers = {
    MyMysqlInitializer.class,
    MySamlKeycloakInitializer.class,
    PortInitializer.class
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext // needed to reuse port 8080 for multiple tests
public class SamlAuthIntegrationTest extends ContainerConfig {

    public final static String CBIO_URL_FROM_BROWSER =
        String.format("http://host.testcontainers.internal:%d", CBIO_PORT);
    
    @Test
    public void a_loginSuccess() {
        Util.testLogin(CBIO_URL_FROM_BROWSER, chromedriverContainer);
    }
   
    @Test
    public void b_testAuthorizedStudy() {
        Util.testLoginAndVerifyStudyNotPresent(CBIO_URL_FROM_BROWSER,chromedriverContainer );
    }

    @Test
    public void c_logoutSuccess() {
        Util.testLogout(CBIO_URL_FROM_BROWSER, chromedriverContainer);
    }

}
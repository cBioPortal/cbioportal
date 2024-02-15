package org.cbioportal.test.integration.security;

import org.cbioportal.PortalApplication;
import org.cbioportal.test.integration.security.util.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.cbioportal.test.integration.security.ContainerConfig.MyMysqlInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.MyOAuth2KeycloakInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.PortInitializer;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {PortalApplication.class}
)
@TestPropertySource(
    properties = {
        "authenticate=oauth2",
        "dat.method=oauth2",
        // DB settings (also see MysqlInitializer)
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // OAuth2 settings
        "spring.security.oauth2.client.provider.keycloak.user-name-attribute=email",
        "spring.security.oauth2.client.registration.keycloak.client-name=cbioportal_oauth2",
        "spring.security.oauth2.client.registration.keycloak.client-id=cbioportal_oauth2",
        "spring.security.oauth2.client.registration.keycloak.client-secret=client_secret",
        "spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code",
        "spring.security.oauth2.client.registration.keycloak.client-authentication-method=client_secret_post",
        "spring.security.oauth2.client.registration.keycloak.scope=openid,email,roles",
        "spring.security.oauth2.client.jwt-roles-path=resource_access::cbioportal::roles",
        // Keycloak host settings (also see KeycloakInitializer)
        "dat.oauth2.clientId=cbioportal_oauth2",
        "dat.oauth2.clientSecret=client_secret",
        // Redirect URL to cBiopPortal application from perspective of browser
        "dat.oauth2.redirectUri=http://localhost:8080/api/data-access-token/oauth2",
        "dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles",
        "session.service.url=http://localhost:5000/api/sessions/my_portal/",
        "filter_groups_by_appname=false"

    }
)
@ContextConfiguration(initializers = {
	MyMysqlInitializer.class,
    MyOAuth2KeycloakInitializer.class,
    PortInitializer.class
})
@DirtiesContext
public class OAuth2AuthIntegrationTest extends ContainerConfig {

    public final static String CBIO_URL_FROM_BROWSER =
        String.format("http://localhost:%d", CBIO_PORT);   
    
    @Test
    public void a_loginSuccess() {
        Util.testLogin(CBIO_URL_FROM_BROWSER, chromeDriver);
    }
    
    @Test
    public void b_downloadOfflineToken() throws Exception {
        Util.testDownloadOfflineToken(CBIO_URL_FROM_BROWSER, chromeDriver);
    }

    @Test
    public void c_logoutSuccess() {
        Util.testOAuthLogout(CBIO_URL_FROM_BROWSER, chromeDriver);
    }

    @Test
    public void d_loginAgainSuccess() {
        Util.testLoginAgain(CBIO_URL_FROM_BROWSER, chromeDriver);
    }
    
}
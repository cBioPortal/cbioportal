/*
 * Copyright (c) 2019 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.test.integration.security;


import static org.cbioportal.test.integration.security.util.TokenHelper.encodeWithoutSigning;


import java.io.IOException;
import java.net.URLEncoder;
import org.cbioportal.PortalApplication;
import org.cbioportal.test.integration.SharedMysqlContainer;
import org.cbioportal.test.integration.security.util.HttpHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Tests SAML authentication and offline token download
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {PortalApplication.class}
)
@TestPropertySource(
    properties = {
        "app.name=cbioportal",
        "filter_groups_by_appname=true",
        "authenticate=saml",
        "dat.method=oauth2",
        // DB settings
        // note: DB_URL, DB_USERNAME, and DB_PASSWORD are set by SharedMysqlContainer
        "spring.datasource.url=${DB_URL}",
        "spring.datasource.username=${DB_USERNAME}",
        "spring.datasource.password=${DB_PASSWORD}",
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // SAML settings
        "saml.keystore.location=classpath:/security/testSamlKeystore.jks",
        "saml.keystore.password=123456",
        "saml.keystore.private-key.key=secure-key",
        "saml.keystore.private-key.password=654321",
        "saml.keystore.default-key=secure-key",
        "saml.idp.metadata.location=classpath:/security/saml-idp-metadata.xml",
        // I had to use specificBinding because of this bug https://github.com/spring-projects/spring-security-saml/issues/460
        "saml.idp.comm.binding.settings=specificBinding",
        "saml.idp.comm.binding.type=bindings:HTTP-Redirect",
        "saml.sp.metadata.entitybaseurl=#{null}",
        "saml.sp.metadata.entityid=cbioportal",
        "saml.idp.metadata.entityid=spring.security.saml.idp.id",
        "saml.idp.metadata.attribute.email=User.email",
        // TODO what is this prop?
        "saml.logout.local=false",
        // FIXME Our test saml idp does not sign assertions for some reason
        "saml.sp.metadata.wantassertionsigned=false",
        "dat.oauth2.clientId=client_id",
        "dat.oauth2.clientSecret=client_secret",
        "dat.oauth2.issuer=token_issuer",
        "dat.oauth2.accessTokenUri=http://localhost:8081/auth/realms/cbio/token",
        "dat.oauth2.redirectUri=http://localhost:8080/api/data-access-token/oauth2",
        "dat.oauth2.userAuthorizationUri=http://localhost:8081/auth/realms/cbio/auth",
        "dat.oauth2.jwkUrl=http://localhost:8081/auth/realms/cbio/jwkUrl",
        "dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles"
    }
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OfflineTokenDownloadIntegrationTest {

    private static String CBIO_URL = "http://localhost:8080";
    private static final int MOCKSERVER_PORT = 8081;
    private static final int IDP_PORT = 8082;
    private static final String IDP_URL = String.format("http://localhost:%d", IDP_PORT);

    @ClassRule
    public static SharedMysqlContainer mysqlContainer = SharedMysqlContainer.getInstance();

    private static String cbioCookie;
    //FIXME Endpoints to download offline token do not initiate login with saml, their simply return 401 http code. Should it be this way? Add tests

    @Test
    public void A_testReachHomePageOnlyAfterLogInWithSamlIdp() throws IOException {
        //1. When we try to reach cbioportal
        String cbioPageUrl = CBIO_URL + "/";
        HttpHelper.HttpResponse discoveryEndpointRedirect = HttpHelper.sendGetRequest(cbioPageUrl, null, null);
        //1. Then we get redirect to the discovery page
        Assertions.assertEquals(302, discoveryEndpointRedirect.code);
        String discoveryEndpointLocation = discoveryEndpointRedirect.headers.get("Location").get(0);
        Assertions.assertEquals(CBIO_URL + "/saml2/authenticate/cbioportal_saml_idp", discoveryEndpointLocation);
        //1. And we set the session cookie
        String cbioSetCookie = discoveryEndpointRedirect.headers.get("Set-Cookie").get(0);
        Assertions.assertTrue(cbioSetCookie.startsWith("JSESSIONID="));
        cbioCookie = cbioSetCookie.split(";")[0];

        //2. When we make a request to the discovery endpoint
        HttpHelper.HttpResponse cbioIdpLoginRedirect = HttpHelper.sendGetRequest(discoveryEndpointLocation, null, cbioCookie);
        //2. Then it redirects us to the saml idp login screen of the cbioportal
        Assertions.assertEquals(302, cbioIdpLoginRedirect.code);
        String cbioIdpLoginLocation = cbioIdpLoginRedirect.headers.get("Location").get(0);
        Assertions.assertTrue(cbioIdpLoginLocation.startsWith(IDP_URL + "/saml/idp/SSO/alias/boot-sample-idp"));

        //3. When we make a request to the SAML Single Sign On endpoint
        HttpHelper.HttpResponse idpRedirect = HttpHelper.sendGetRequest(cbioIdpLoginLocation, null, cbioCookie);
        //3. Then we get redirected to the saml idp site
        Assertions.assertEquals(302, idpRedirect.code);
        String idpLocation = idpRedirect.headers.get("Location").get(0);
        Assertions.assertTrue(idpLocation.startsWith(IDP_URL + "/login"));

        //4. When we make a request to the idp page
        HttpHelper.HttpResponse idpLoginRedirect = HttpHelper.sendGetRequest(idpLocation, null, null);
        //4. Then we get redirected to the saml idp login page
        Assertions.assertEquals(200, idpLoginRedirect.code);
//        String idpLoginLocation = idpLoginRedirect.headers.get("Location").get(0);
//        Assertions.assertTrue(idpLoginLocation.startsWith(IDP_URL + "/login"));
//        //4. And we set the idp session cookie
//        String idpSetCookie = idpLoginRedirect.headers.get("Set-Cookie").get(0);
//        Assertions.assertTrue(idpSetCookie.startsWith("JSESSIONID="));
//        String idpCookie = idpSetCookie.split(";")[0];

        // We skipped requesting the login page for the brevity

        //5. When we submit the login form
        HttpHelper.HttpResponse idpLoginReponse = HttpHelper.sendPostRequest(idpLocation, null, null, "username=user&password=password");
        //5. Then we get redirected to the saml idp site
        Assertions.assertEquals(302, idpLoginReponse.code);
        String jumpToServiceProviderPageLocation = idpLoginReponse.headers.get("Location").get(0);
        Assertions.assertTrue(jumpToServiceProviderPageLocation.startsWith(IDP_URL + "/"));
        //5. And we set the idp session cookie
        String idpSetCookie = idpLoginReponse.headers.get("Set-Cookie").get(0);
        Assertions.assertTrue(idpSetCookie.startsWith("JSESSIONID="));
        String idpCookie = idpSetCookie.split(";")[0];

        //6. When we reach the jump page
        HttpHelper.HttpResponse jumpToServiceProviderPageReponse = HttpHelper.sendGetRequest(jumpToServiceProviderPageLocation, null, idpCookie);

        


        //6. Then we get html page with javascript that redirects us to the service provider
        Assertions.assertEquals(200, jumpToServiceProviderPageReponse.code);
        String jumpPage = jumpToServiceProviderPageReponse.body;
        Assertions.assertTrue(jumpPage.contains("form action=\"" + CBIO_URL + "/saml/SSO\""));
        String samlResponseValueStart = "name=\"SAMLResponse\" value=\"";
        Assertions.assertTrue(jumpPage.contains(samlResponseValueStart));
        int start = jumpPage.indexOf(samlResponseValueStart);
        int end = jumpPage.indexOf("\"", start + samlResponseValueStart.length());
        String samlResponse = jumpPage.substring(start + samlResponseValueStart.length(), end);

        //7. When we submit the assertions to the consumer
        HttpHelper.HttpResponse requestAssertionsConsumerRepsonse = HttpHelper
            .sendPostRequest(CBIO_URL + "/saml/SSO", null, cbioCookie,
                "SAMLResponse=" + URLEncoder.encode(samlResponse, "UTF-8"));
        //7. Then we get redirected to originally requested page
        Assertions.assertEquals(302, requestAssertionsConsumerRepsonse.code);
        String dataAccessTokenLocation = requestAssertionsConsumerRepsonse.headers.get("Location").get(0);
        
        Assertions.assertEquals("/cbioportal/restore?key=login-redirect", dataAccessTokenLocation);

        //8. Finally we can reach the home page
        HttpHelper.HttpResponse homePageResponse = HttpHelper.sendGetRequest(cbioPageUrl, null, cbioCookie);
        Assertions.assertEquals(200, homePageResponse.code);
        Assertions.assertFalse(homePageResponse.body.isEmpty());

    }

    @Test
    public void B_testDownloadOfflineToken() throws IOException {
        String offlineTokenClaims = "{\"sub\": \"1234567890\"}";
        String encodedOfflineTokenClaims = encodeWithoutSigning(offlineTokenClaims);
        new MockServerClient("localhost", IDP_PORT).when(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(StringBody.subString("code=code1")))
            .respond(
                HttpResponse.response()
                    .withBody("{\"refresh_token\": \""
                        + encodedOfflineTokenClaims
                        + "\"}"));

        HttpHelper.HttpResponse offlineTokenResponse = HttpHelper.sendGetRequest(CBIO_URL + "/api/data-access-token/oauth2?code=code1", null, cbioCookie);

        Assertions.assertEquals(200, offlineTokenResponse.code);
        Assertions.assertTrue(offlineTokenResponse.headers.get("Content-Disposition").get(0).startsWith("attachment; filename="));
        Assertions.assertEquals("token: " + encodedOfflineTokenClaims, offlineTokenResponse.body);
    }

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(MOCKSERVER_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }
}

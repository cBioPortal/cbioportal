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


import static org.cbioportal.test.integration.security.Oauth2ResourceServerIntegrationTest.*;
import static org.cbioportal.test.integration.security.util.TokenHelper.encodeWithoutSigning;
import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.net.URLEncoder;
import org.cbioportal.PortalApplication;
import org.cbioportal.test.integration.MysqlInitializer;
import org.cbioportal.test.integration.SharedMysqlContainer;
import org.cbioportal.test.integration.security.util.HttpHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Tests protection of API endpoints by SAML auth
 */
// This starts a live portal instance on a random port (imported via @LocalServerPort)
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
        // DB settings
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // SAML settings
        "saml.keystore.location=classpath:/security/samlKeystore.jks",
        "saml.keystore.password=P@ssword1",
        "saml.keystore.private-key.key=secure-key",
        "saml.keystore.private-key.password=P@ssword1",
        "saml.keystore.default-key=secure-key",
        "saml.idp.metadata.location=classpath:/security/saml-idp-metadata.xml",
        // I had to use specificBinding because of this bug https://github.com/spring-projects/spring-security-saml/issues/460
        "saml.idp.comm.binding.settings=specificBinding",
        "saml.idp.comm.binding.type=bindings:HTTP-Redirect",
        "saml.sp.metadata.entitybaseurl=#{null}",
        "saml.sp.metadata.entityid=cbioportal",
        "saml.idp.metadata.entityid=spring.security.saml.idp.id",
        "saml.idp.metadata.attribute.email=User.email",
        "saml.custom.userservice.class=org.cbioportal.security.spring.authentication.saml.SAMLUserDetailsServiceImpl",
        "saml.logout.local=false",
        // FIXME Our test saml idp does not sign assertions for some reason
        "saml.sp.metadata.wantassertionsigned=false",
        "dat.oauth2.clientId=client_id",
        "dat.oauth2.clientSecret=client_secret",
        "dat.oauth2.issuer=token_issuer",
        "dat.oauth2.accessTokenUri=http://localhost:8443/auth/realms/cbio/token",
        "dat.oauth2.redirectUri=http://localhost:8080/api/data-access-token/oauth2",
        "dat.oauth2.userAuthorizationUri=http://localhost:8443/auth/realms/cbio/auth",
        "dat.oauth2.jwkUrl=http://localhost:8443/auth/realms/cbio/jwkUrl",
        "dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles"
    }
)
@ContextConfiguration(initializers = {
    MyMysqlInitializer.class,
})
@Ignore
public class Oauth2ResourceServerIntegrationTest {
    
    private static String HOST = "http://localhost:8080";
    private static final int IDP_PORT = 8443;

    @ClassRule
    public static SharedMysqlContainer mysqlContainer = SharedMysqlContainer.getInstance();

    // Update application properties with connection info on Mysql container
    public static class MyMysqlInitializer extends MysqlInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, mysqlContainer);
        }
    }
    
    @Test
    public void testAccessForbiddenForAnonymousUser() throws IOException {
        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", null, null);
        assertEquals(401, response.code);
    }

    @Test
    public void testAccessForbiddenForFakeBearerToken() throws IOException {
        String offlineToken = "{\"sub\": \"0000000000\"}";
        String encodedOfflineToken = encodeWithoutSigning(offlineToken);
//        new MockServerClient("localhost", IDP_PORT).when(
//            HttpRequest.request()
//                .withMethod("POST")
//                .withPath("/auth/realms/cbio/token")
//                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
//            .respond(HttpResponse.response().withStatusCode(401));

        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", encodedOfflineToken, null);

        assertEquals(401, response.code);
    }

    @Test
    public void testAccessForValidBearerToken() throws IOException, JSONException {
        String offlineTokenClaims = "{\"sub\": \"1234567890\"}";
        String encodedOfflineToken = encodeWithoutSigning(offlineTokenClaims);
        String accessTokenClaims = "{" +
            "\"sub\": \"1234567890\"," +
            "\"name\": \"John Doe\"," +
            "\"resource_access\": {\"cbioportal\": {\"roles\": [\"cbioportal:study_tcga_pub\"]}}" +
            "}";
//        new MockServerClient("localhost", IDP_PORT).when(
//            HttpRequest.request()
//                .withMethod("POST")
//                .withPath("/auth/realms/cbio/token")
//                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
//            .respond(
//                HttpResponse.response()
//                    .withBody("{\"access_token\": \""
//                        + encodeWithoutSigning(accessTokenClaims)
//                        + "\"}"));

        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", encodedOfflineToken, null);

        assertEquals(200, response.code);
        Assertions.assertTrue(response.body != null && !response.body.isEmpty());
        JSONArray studies = new JSONArray(response.body);
        Assertions.assertEquals(1, studies.length());
        studies.getJSONObject(0).getString("studyId");
        Assertions.assertEquals("study_tcga_pub", studies.getJSONObject(0).getString("studyId"));
    }

    //private static ClientAndServer mockServer;

    //@BeforeClass
    //public static void startServer() {
    //    mockServer = ClientAndServer.startClientAndServer(IDP_PORT);
    //}

    //@AfterClass
    //public static void stopServer() {
    //    mockServer.stop();
    //}
}

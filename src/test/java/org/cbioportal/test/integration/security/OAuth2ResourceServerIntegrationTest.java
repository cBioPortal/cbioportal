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


import org.cbioportal.test.integration.security.util.HttpHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URLEncoder;

import static org.cbioportal.test.integration.security.ContainerConfig.MyMysqlInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.MyOAuth2ResourceServerKeycloakInitializer;
import static org.cbioportal.test.integration.security.util.TokenHelper.encodeWithoutSigning;
import static org.junit.Assert.assertEquals;


/**
 * Tests protection of API endpoints by OIDC data access token
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@TestPropertySource(
    properties = {
        "authenticate=saml",
        "dat.method=oauth2",
        // DB settings
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
        // FIXME Our test saml idp does not sign assertions for some reason
        "spring.security.saml2.relyingparty.registration.keycloak.entity-id=cbioportal",
        "spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].certificate-location=classpath:dev/security/signing-cert.pem",
        "spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].private-key-location=classpath:dev/security/signing-key.pem", 
        "dat.oauth2.clientId=client_id",
        "dat.oauth2.clientSecret=client_secret",
        "dat.oauth2.redirectUri=http://localhost:8080/api/data-access-token/oauth2",
        // host is the mock server that fakes the oidc idp
        "dat.oauth2.accessTokenUri=http://localhost:8085/realms/cbio/protocol/openid-connect/token",
        "dat.oauth2.userAuthorizationUri=http://localhost:8085/realms/cbio/protocol/openid-connect/auth",
        "dat.oauth2.jwtRolesPath=resource_access::cbioportal::roles",
        "filter_groups_by_appname=false"
    }
)
@ContextConfiguration(initializers = {
    MyMysqlInitializer.class,
    MyOAuth2ResourceServerKeycloakInitializer.class
})
@DirtiesContext
public class OAuth2ResourceServerIntegrationTest extends ContainerConfig {

    public final static String CBIO_URL_FROM_BROWSER =
        String.format("http://localhost:%d", CBIO_PORT);
    
    private final static String tokenUriPath = "/realms/cbio/protocol/openid-connect/token";

    @Test
    public void testAccessForbiddenForAnonymousUser() throws IOException {
        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(CBIO_URL_FROM_BROWSER + "/api/studies", null, null);
        assertEquals(401, response.code);
    }

    @Test
    public void testAccessForbiddenForFakeBearerToken() throws IOException {
        MockServerClient mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getFirstMappedPort());
        String offlineToken = "{\"sub\": \"0000000000\"}";
        String encodedOfflineToken = encodeWithoutSigning(offlineToken);
        mockServerClient.when(
            HttpRequest.request()
                .withMethod("POST")
                .withPath(tokenUriPath)
                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(HttpResponse.response().withStatusCode(401));
        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(CBIO_URL_FROM_BROWSER + "/api/studies", encodedOfflineToken, null);
        assertEquals(401, response.code);
    }

    @Test
    public void testAccessForValidBearerToken() throws IOException, JSONException {
        
        String offlineTokenClaims = "{\"sub\": \"1234567890\"}";
        String encodedOfflineToken = encodeWithoutSigning(offlineTokenClaims);
        String accessTokenClaims = "{" +
            "\"sub\": \"1234567890\"," +
            "\"name\": \"John Doe\"," +
            "\"resource_access\": {\"cbioportal\": {\"roles\": [\"study_tcga_pub\"]}}" +
            "}";
        MockServerClient mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getFirstMappedPort());
        mockServerClient.when(
            HttpRequest.request()
                .withMethod("POST")
                .withPath(tokenUriPath)
                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(
                HttpResponse.response()
                    .withBody("{\"access_token\": \""
                        + encodeWithoutSigning(accessTokenClaims)
                        + "\"}"));
        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(CBIO_URL_FROM_BROWSER + "/api/studies", encodedOfflineToken, null);

        assertEquals(200, response.code);
        Assertions.assertTrue(response.body != null && !response.body.isEmpty());
        JSONArray studies = new JSONArray(response.body);
        Assertions.assertEquals(1, studies.length());
        studies.getJSONObject(0).getString("studyId");
        Assertions.assertEquals("study_tcga_pub", studies.getJSONObject(0).getString("studyId"));
    }

}

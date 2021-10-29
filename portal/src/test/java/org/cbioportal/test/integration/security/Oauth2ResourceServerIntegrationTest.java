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
import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.net.URLEncoder;
import org.cbioportal.test.integration.security.util.HttpHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;


/**
 * Tests protection of API endpoints
 */
public class Oauth2ResourceServerIntegrationTest {

    private static final String HOST = "http://localhost:8080/cbioportal";
    private static final int IDP_PORT = 8443;

    @Test
    public void testAccessForbiddenForAnonymousUser() throws IOException {
        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", null, null);
        assertEquals(401, response.code);
    }

    @Test
    public void testAccessForbiddenForFakeBearerToken() throws IOException {
        String offlineToken = "{\"sub\": \"0000000000\"}";
        String encodedOfflineToken = encodeWithoutSigning(offlineToken);
        new MockServerClient("localhost", IDP_PORT).when(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(HttpResponse.response().withStatusCode(401));

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
        new MockServerClient("localhost", IDP_PORT).when(
            HttpRequest.request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(StringBody.subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(
                HttpResponse.response()
                    .withBody("{\"access_token\": \""
                        + encodeWithoutSigning(accessTokenClaims)
                        + "\"}"));

        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", encodedOfflineToken, null);

        assertEquals(200, response.code);
        Assertions.assertTrue(response.body != null && !response.body.isEmpty());
        JSONArray studies = new JSONArray(response.body);
        Assertions.assertEquals(1, studies.length());
        studies.getJSONObject(0).getString("studyId");
        Assertions.assertEquals("study_tcga_pub", studies.getJSONObject(0).getString("studyId"));
    }

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(IDP_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }
}

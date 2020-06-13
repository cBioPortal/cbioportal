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

package org.mskcc.cbio.portal;


import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.subString;
import static org.mskcc.cbio.portal.TokenHelper.encodeWithoutSigning;


/**
 * Tests protection of API endpoints
 */
public class Oauth2ResourceServerIntegrationTests {

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
            request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(response().withStatusCode(401));

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
            request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(subString("refresh_token=" + URLEncoder.encode(encodedOfflineToken, "UTF-8"))))
            .respond(
                response()
                    .withBody("{\"access_token\": \""
                        + encodeWithoutSigning(accessTokenClaims)
                        + "\"}"));

        HttpHelper.HttpResponse response = HttpHelper.sendGetRequest(HOST + "/api/studies", encodedOfflineToken, null);

        assertEquals(200, response.code);
        assertTrue(response.body != null && !response.body.isEmpty());
        JSONArray studies = new JSONArray(response.body);
        assertEquals(1, studies.length());
        studies.getJSONObject(0).getString("studyId");
        assertEquals("study_tcga_pub", studies.getJSONObject(0).getString("studyId"));
    }

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(IDP_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }
}

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


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.net.URLEncoder;

import static org.junit.Assert.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.subString;
import static org.mskcc.cbio.portal.TokenHelper.encodeWithoutSigning;


/**
 * Tests SAML authentication and offline token download
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OfflineTokenDownloadIntegrationTests {

    private static final String HOST = "http://localhost:8080";
    private static final String CBIO_URL = HOST + "/cbioportal";
    private static final String IDP_URL = HOST + "/saml-idp";

    private static final int IDP_PORT = 8443;

    private static String cbioCookie;
    //FIXME Endpoints to download offline token do not initiate login with saml, their simply return 401 http code. Should it be this way? Add tests

    @Test
    public void A_testReachHomePageOnlyAfterLogInWithSamlIdp() throws IOException {
        //1. When we try to reach cbioportal
        String cbioPageUrl = CBIO_URL + "/";
        HttpHelper.HttpResponse discoveryEndpointRedirect = HttpHelper.sendGetRequest(cbioPageUrl, null, null);
        //1. Then we get redirect to the discovery page
        assertEquals(302, discoveryEndpointRedirect.code);
        String discoveryEndpointLocation = discoveryEndpointRedirect.headers.get("Location").get(0);
        assertEquals(CBIO_URL + "/saml/discovery?entityID=cbioportal&returnIDParam=idp", discoveryEndpointLocation);
        //1. And we set the session cookie
        String cbioSetCookie = discoveryEndpointRedirect.headers.get("Set-Cookie").get(0);
        assertTrue(cbioSetCookie.startsWith("JSESSIONID="));
        cbioCookie = cbioSetCookie.split(";")[0];

        //2. When we make a request to the discovery endpoint
        HttpHelper.HttpResponse cbioIdpLoginRedirect = HttpHelper.sendGetRequest(discoveryEndpointLocation, null, cbioCookie);
        //2. Then it redirects us to the saml idp login screen of the cbioportal
        assertEquals(302, cbioIdpLoginRedirect.code);
        String cbioIdpLoginLocation = cbioIdpLoginRedirect.headers.get("Location").get(0);
        assertEquals(CBIO_URL + "/saml/login?disco=true&idp=spring.security.saml.idp.id", cbioIdpLoginLocation);

        //3. When we make a request to the cbio login page
        HttpHelper.HttpResponse idpRedirect = HttpHelper.sendGetRequest(cbioIdpLoginLocation, null, cbioCookie);
        //3. Then we get redirected to the saml idp site
        assertEquals(302, idpRedirect.code);
        String idpLocation = idpRedirect.headers.get("Location").get(0);
        assertTrue(idpLocation.startsWith(IDP_URL + "/saml/idp/SSO/alias/boot-sample-idp?"));

        //4. When we make a request to the idp page
        HttpHelper.HttpResponse idpLoginRedirect = HttpHelper.sendGetRequest(idpLocation, null, null);
        //4. Then we get redirected to the saml idp login page
        assertEquals(302, idpLoginRedirect.code);
        String idpLoginLocation = idpLoginRedirect.headers.get("Location").get(0);
        assertTrue(idpLoginLocation.startsWith(IDP_URL + "/login"));
        //4. And we set the idp session cookie
        String idpSetCookie = idpLoginRedirect.headers.get("Set-Cookie").get(0);
        assertTrue(idpSetCookie.startsWith("JSESSIONID="));
        String idpCookie = idpSetCookie.split(";")[0];

        // We skipped requesting the login page for the brevity

        //5. When we submit the login form
        HttpHelper.HttpResponse idpLoginRepsonse = HttpHelper.sendPostRequest(idpLoginLocation, null, idpCookie, "username=user&password=password");
        //5. Then we get redirected to the saml idp site
        assertEquals(302, idpLoginRepsonse.code);
        String jumpToServiceProviderPageLocation = idpLoginRepsonse.headers.get("Location").get(0);
        assertTrue(jumpToServiceProviderPageLocation.startsWith(IDP_URL + "/saml/idp/SSO/alias/boot-sample-idp?"));
        //5. And we set the idp session cookie
        idpSetCookie = idpLoginRepsonse.headers.get("Set-Cookie").get(0);
        assertTrue(idpSetCookie.startsWith("JSESSIONID="));
        idpCookie = idpSetCookie.split(";")[0];

        //6. When we reach the jump page
        HttpHelper.HttpResponse jumpToServiceProviderPageRepsonse = HttpHelper.sendGetRequest(jumpToServiceProviderPageLocation, null, idpCookie);
        //6. Then we get html page with javascript that redirects us to the service provider
        assertEquals(200, jumpToServiceProviderPageRepsonse.code);
        String jumpPage = jumpToServiceProviderPageRepsonse.body;
        assertTrue(jumpPage.contains("form action=\"" + CBIO_URL + "/saml/SSO\""));
        String samlResponseValueStart = "name=\"SAMLResponse\" value=\"";
        assertTrue(jumpPage.contains(samlResponseValueStart));
        int start = jumpPage.indexOf(samlResponseValueStart);
        int end = jumpPage.indexOf("\"", start + samlResponseValueStart.length());
        String samlResponse = jumpPage.substring(start + samlResponseValueStart.length(), end);

        //7. When we submit the assertions to the consumer
        HttpHelper.HttpResponse requestAssertionsConsumerRepsonse = HttpHelper
            .sendPostRequest(CBIO_URL + "/saml/SSO", null, cbioCookie,
                "SAMLResponse=" + URLEncoder.encode(samlResponse, "UTF-8"));
        //7. Then we get redirected to originally requested page
        assertEquals(302, requestAssertionsConsumerRepsonse.code);
        String dataAccessTokenLocation = requestAssertionsConsumerRepsonse.headers.get("Location").get(0);
        
        assertEquals("/cbioportal/restore?key=login-redirect", dataAccessTokenLocation);

        //8. Finally we can reach the home page
        HttpHelper.HttpResponse homePageResponse = HttpHelper.sendGetRequest(cbioPageUrl, null, cbioCookie);
        assertEquals(200, homePageResponse.code);
        assertFalse(homePageResponse.body.isEmpty());

    }

    @Test
    public void B_testDownloadOfflineToken() throws IOException {
        String offlineTokenClaims = "{\"sub\": \"1234567890\"}";
        String encodedOfflineTokenClaims = encodeWithoutSigning(offlineTokenClaims);
        new MockServerClient("localhost", IDP_PORT).when(
            request()
                .withMethod("POST")
                .withPath("/auth/realms/cbio/token")
                .withBody(subString("code=code1")))
            .respond(
                response()
                    .withBody("{\"refresh_token\": \""
                        + encodedOfflineTokenClaims
                        + "\"}"));

        HttpHelper.HttpResponse offlineTokenResponse = HttpHelper.sendGetRequest(CBIO_URL + "/api/data-access-token/oauth2?code=code1", null, cbioCookie);

        assertEquals(200, offlineTokenResponse.code);
        assertTrue(offlineTokenResponse.headers.get("Content-Disposition").get(0).startsWith("attachment; filename="));
        assertEquals("token: " + encodedOfflineTokenClaims, offlineTokenResponse.body);
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

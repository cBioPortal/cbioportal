/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.cbioportal.security.spring.authentication.token.oauth2.OAuth2TokenTestUtils.createJwt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.cbioportal.model.DataAccessToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@TestPropertySource(
    properties = {
        "dat.method = oauth2",
        "dat.oauth2.issuer = my_issuer",
        "dat.oauth2.clientId = my_client_id"
    }
)
@ContextConfiguration(classes=OAuth2DataAccessTokenServiceImplTestConfiguration.class)
@RunWith(SpringRunner.class)
public class OAuth2DataAccessTokenServiceImplTest {

    static final String ACCESS_CODE_VALID = "dummy_valid_access_code";
    static final String ACCESS_CODE_INVALID = "dummy_invalid_access_code";
    static final String ACCESS_TOKEN = "dummy_access_token";

    @Value("${dat.oauth2.issuer:}")
    private String issuer;

    @Value("${dat.oauth2.clientId:}")
    private String clientId;

    @Autowired
    OAuth2DataAccessTokenServiceImpl service;

    @Test
    public void testCreateDataAccessTokenSuccess() {
        DataAccessToken accessToken = service.createDataAccessToken(ACCESS_CODE_VALID);
        assertEquals(accessToken.getToken(), ACCESS_TOKEN);
    }

    @Test(expected = BadCredentialsException.class)
    public void testCreateDataAccessTokenFailure() {
        service.createDataAccessToken(ACCESS_CODE_INVALID);
    }

    @Test
    public void testGetUsernameSuccess() {
        String token = createJwt("{\"sub\":\"me\"}");
        assertEquals(service.getUsername(token), "me");
    }

    @Test(expected = BadCredentialsException.class)
    public void testGetUsernameFailure() {
        String token = createJwt("{}");
        service.getUsername(token);
    }

    @Test
    public void testCreateAuthenticationRequestSuccess() {
        String token = createJwt("{\"sub\":\"me\"}");
        Authentication auth = service.createAuthenticationRequest(token);
        assertEquals(auth.getPrincipal(), "me");
        assertEquals(auth.getCredentials(), token);
    }

    @Test(expected = BadCredentialsException.class)
    public void testCreateAuthenticationRequestFailure() {
        String token = createJwt("{}");
        service.createAuthenticationRequest(token);
    }

    @Test
    public void testGetExpirationReturnsNull() {
        assertNull(service.getExpiration("dummy_token"));
    }

    @Test
    public void testIsValidSuccess() {
        String json = new StringBuilder()
            .append("{")
                .append("\"aud\":\"").append(clientId).append("\",")
                .append("\"iss\":\"").append(issuer).append("\"")
            .append("}")
            .toString();

        String token = createJwt(json);

        assert(service.isValid(token));
    }

    @Test(expected = BadCredentialsException.class)
    public void testIsValidFailureAud() {
        String json = new StringBuilder()
            .append("{")
                .append("\"aud\":\"").append("invalid_client_id").append("\",")
                .append("\"iss\":\"").append(issuer).append("\"")
            .append("}")
            .toString();

        String token = createJwt(json);

        service.isValid(token);
    }

    @Test(expected = BadCredentialsException.class)
    public void testIsValidFailureIss() {
        String json = new StringBuilder()
            .append("{")
                .append("\"aud\":\"").append(clientId).append("\",")
                .append("\"iss\":\"").append("invalid_issuer").append("\"")
            .append("}")
            .toString();

        String token = createJwt(json);

        service.isValid(token);
    }

}
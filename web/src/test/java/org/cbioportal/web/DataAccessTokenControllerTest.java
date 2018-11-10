/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.web;

import java.util.*;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class DataAccessTokenControllerTest {
    @Autowired
    private DataAccessTokenService tokenService;

    @Autowired
    private DataAccessTokenController dataAccessTokenController;

    @Bean
    public DataAccessTokenService tokenService() {
        return Mockito.mock(DataAccessTokenService.class);
    }

    @Bean
    public DataAccessTokenController dataAccessTokenController() {
        return new DataAccessTokenController();
    }

    public static final String API_TEST_SUBJECT = "testSubject";
    public static final String NON_API_TEST_SUBJECT = "anonymousUser";
    public static final String MOCK_TOKEN_STRING = "MockedTokenString";
    public static final DataAccessToken MOCK_TOKEN_INFO = new DataAccessToken(MOCK_TOKEN_STRING);

    @Before
    public void setup() {
        Mockito.reset(tokenService);
    }

    protected Authentication makePrincipal(String username, boolean isAuthentic) {
        User user = new User(username, "unused", new ArrayList<GrantedAuthority>());
        if (isAuthentic) {
            return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        } else {
            return new UsernamePasswordAuthenticationToken(user, "");
        }
    }

    @Test
    public void createTokenValidUserTest() throws Exception {
        Mockito.when(tokenService.createDataAccessToken(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(MOCK_TOKEN_INFO);
        Authentication principal = this.makePrincipal(API_TEST_SUBJECT, true);
        ResponseEntity<DataAccessToken> serviceResponse = dataAccessTokenController.createDataAccessToken(principal, Boolean.FALSE);
        checkIfResponseMatches(serviceResponse, HttpStatus.CREATED, MOCK_TOKEN_INFO);
    }

    @Test(expected = DataAccessTokenNoUserIdentityException.class)
    public void createTokenInvalidUserTest() throws Exception {
        Mockito.when(tokenService.createDataAccessToken(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(MOCK_TOKEN_INFO);
        Authentication principal = this.makePrincipal(API_TEST_SUBJECT, false);
        ResponseEntity<DataAccessToken> serviceResponse = dataAccessTokenController.createDataAccessToken(principal, Boolean.FALSE);
    }

    @Test(expected = DataAccessTokenProhibitedUserException.class)
    public void createTokenNonAPIUserTest() throws Exception {
        Mockito.when(tokenService.createDataAccessToken(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(MOCK_TOKEN_INFO);
        Authentication principal = this.makePrincipal(NON_API_TEST_SUBJECT, true);
        ResponseEntity<DataAccessToken> serviceResponse = dataAccessTokenController.createDataAccessToken(principal, Boolean.FALSE);
    }

    private void checkIfResponseMatches(ResponseEntity<DataAccessToken> response, HttpStatus expectedStatus, DataAccessToken expectedResponseBody) {
        HttpStatus responseStatus = response.getStatusCode();
        if (responseStatus != expectedStatus) {
            Assert.fail("Response from controller handler (" + responseStatus + ") did not match expected status : " + expectedStatus);
        }
        DataAccessToken token = response.getBody();
        String tokenString = token.getToken();
        String expectedTokenString = expectedResponseBody.getToken();
        if (tokenString == null && expectedTokenString == null) {
            return;
        }
        if (tokenString == null || !tokenString.equals(expectedTokenString)) {
            Assert.fail("Response from controller (" + tokenString + ") did not contain the expected response body : " + expectedTokenString);
        }
    }

    // Test: retrieve a token via GET to dataAccessToken/{token} - [fails]
    @Test(expected = UnsupportedOperationException.class)
    public void getDataAccessTokenInfoTest() throws Exception {
        Mockito.doThrow(new UnsupportedOperationException()).when(tokenService).getDataAccessTokenInfo(Matchers.anyString());
        ResponseEntity<DataAccessToken> token = dataAccessTokenController.getDataAccessToken(MOCK_TOKEN_STRING);
    }

    // Test: retrieve tokens via GET to dataAccessToken - return all tokens associated with the user [fails]
    @Test(expected = UnsupportedOperationException.class)
    public void getAllDataAccessTokensTest() throws Exception {
        Mockito.doThrow(new UnsupportedOperationException()).when(tokenService).getAllDataAccessTokens(Matchers.anyString());
        Authentication principal = this.makePrincipal(API_TEST_SUBJECT, true);
        ResponseEntity<List<DataAccessToken>> tokens = dataAccessTokenController.getAllDataAccessTokens(principal);
    }

    // Test: revoke tokens via DELETE to dataAccessToken - revoke all tokens assiciated with the user [fails]
    @Test(expected = UnsupportedOperationException.class)
    public void revokeAllDataAccessTokensTest() throws Exception {
        Mockito.doThrow(new UnsupportedOperationException()).when(tokenService).revokeAllDataAccessTokens(Matchers.anyString());
        Authentication principal = this.makePrincipal(API_TEST_SUBJECT, true);
        dataAccessTokenController.revokeAllDataAccessTokens(principal);
    }

    // Test: revoke a token via DELETE to dataAccessToken/{token} - [fails]
    @Test(expected = UnsupportedOperationException.class)
    public void revokeDataAccessTokenTest() throws Exception {
        Mockito.doThrow(new UnsupportedOperationException()).when(tokenService).revokeDataAccessToken(Matchers.anyString());
        dataAccessTokenController.revokeDataAccessToken(MOCK_TOKEN_STRING);
    }

}

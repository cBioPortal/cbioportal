/*
 * Copyright (c) 2018-2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.security.spring.authentication.token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.service.util.JwtUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties = { "dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s=",
                    "dat.ttl_seconds = 60", // this will be the default expiration for tokens
                    "dat.method = jwt"
    },
    inheritLocations = false
)
@ContextConfiguration(classes=TokenAuthenticationFilterTestConfiguration.class)
@RunWith(SpringRunner.class)
public class TokenAuthenticationFilterTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    private static final int TEST_TOKEN_EXPIRATION_SECONDS = 1;

    private static final Log LOG = LogFactory.getLog(TokenAuthenticationFilterTest.class);

    // TODO: test requiresValidation() function maybe

    @Test
    public void testAttemptAuthentication_success() {
        String token = jwtUtils.createToken(TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT).getToken();
        LOG.debug("testAttemptAuthentication_success() token = " + token);
        Mockito.reset(request);
        Mockito.when(request.getHeader(ArgumentMatchers.anyString())).thenReturn("Bearer " + token);
        // response object is autowired above
        Authentication authentication = tokenAuthenticationFilter.attemptAuthentication(request, response);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // userDetails is mocked and hard coded to return TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT
        // but we will only get TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT if the token is valid and
        // the authority provider (not mocked) successfully authenticated
        // if the token is invalid or the authentication provider fails then exceptions are thrown
        if (userDetails == null || userDetails.getUsername() == null || !userDetails.getUsername().equalsIgnoreCase(TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT)) {
            Assert.fail("principal username returned by authentication filter (" + userDetails == null ? "null" : userDetails.getUsername() + ") does not match token user : (" + TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT +")");
        }
        Mockito.reset(request);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAttemptAuthentication_nullToken() {
        Mockito.reset(request);
        Mockito.when(request.getHeader(ArgumentMatchers.anyString())).thenReturn(null);
        // response object is autowired above
        tokenAuthenticationFilter.attemptAuthentication(request, response);
        // make sure we call Mockito.reset(request) in other methods
    }

    @Test(expected = BadCredentialsException.class)
    public void testAttemptAuthentication_expiredToken() throws InterruptedException {
        String token = jwtUtils.createToken(TokenAuthenticationFilterTestConfiguration.TEST_SUBJECT, TEST_TOKEN_EXPIRATION_SECONDS).getToken();
        LOG.debug("testAttemptAuthentication_expiredToken() token = " + token);
        Mockito.reset(request);
        Mockito.when(request.getHeader(ArgumentMatchers.anyString())).thenReturn("Bearer " + token);
        Thread.sleep((TEST_TOKEN_EXPIRATION_SECONDS * 1000L) + 10L); // NOTE: sleep time must be adequate to allow created token to expire
        // response object is autowired above
        tokenAuthenticationFilter.attemptAuthentication(request, response);
        // make sure we call Mockito.reset(request) in other methods
    }

}

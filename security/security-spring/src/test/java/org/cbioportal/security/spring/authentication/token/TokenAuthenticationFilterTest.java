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

package org.cbioportal.security.spring.authentication.token;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.util.JwtUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component; // TODO is this the correct one to use?
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

@TestPropertySource(
    properties = { "jwt.secret.key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s=",
                    "jwt.ttl_seconds = 1",
                    "jwt.issuer = org.cbioportal.mskcc.org"
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

    private static final String TEST_SUBJECT = "testSubject";
    private static final long TEST_TOKEN_EXPIRATION_MILLISECONDS = 2000L;

    // TODO: test requiresValidation() function maybe

    @Test
    public void testAttemptAuthentication() {
        String token = jwtUtils.createToken(TEST_SUBJECT);
        Mockito.reset(request);
        Mockito.when(request.getHeader(Matchers.anyString())).thenReturn("Bearer " + token);
        // response object is autowired above
        Authentication authentication = tokenAuthenticationFilter.attemptAuthentication(request, response); 
        String principal = (String)authentication.getPrincipal();
        //TODO : maybe we need a PortalUserDetails Object instead (that is the type we normally work with)
        if (principal == null || !principal.equalsIgnoreCase(TEST_SUBJECT)) {
            Assert.fail("principal returned by authentication filter (" + principal + ") does not match token user : (" + TEST_SUBJECT +")");
        }
        // TODO : check cases for null token, expired token
        Mockito.reset(request);
    }

             


}

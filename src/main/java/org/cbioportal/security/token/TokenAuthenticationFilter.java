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

package org.cbioportal.security.token;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cbioportal.service.DataAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

/**
 *
 * @author Manda Wilson
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private DataAccessTokenService tokenService;

    private static final String BEARER = "Bearer";

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    public TokenAuthenticationFilter() {
        // allow any request to contain an authorization header
        super("/**");
    }

    public TokenAuthenticationFilter(String s, AuthenticationManager authenticationManagerBean) {
        super(s, authenticationManagerBean);
    }
    
    public TokenAuthenticationFilter(String s, AuthenticationManager authenticationManager, DataAccessTokenService tokenService) {
        super(s, authenticationManager);
        this.tokenService = tokenService;
    }
    
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // only required if we do see an authorization header
        String param = request.getHeader(AUTHORIZATION);
        if (param == null) {
            LOG.debug("attemptAuthentication(), authorization header is null, continue on to other security filters");
            return false;
        }
        return true;
    }

    @Override
    public Authentication attemptAuthentication (HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, jakarta.servlet.ServletException{

        String token = extractHeaderToken(request);

        if (token == null) {
            LOG.error("No token was found in request header.");
            throw new BadCredentialsException("No token was found in request header.");
        }

        Authentication auth = tokenService.createAuthenticationRequest(token);

        return getAuthenticationManager().authenticate(auth);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }

    /**
     * Extract the bearer token from a header.
     * 
     * @param request
     * @return The token, or null if no authorization header was supplied
     */
    protected String extractHeaderToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && !authorizationHeader.isEmpty() && authorizationHeader.toLowerCase().startsWith(BEARER.toLowerCase())) {
            return authorizationHeader.substring(BEARER.length()).trim();
        }
        return null;
    }
    
}

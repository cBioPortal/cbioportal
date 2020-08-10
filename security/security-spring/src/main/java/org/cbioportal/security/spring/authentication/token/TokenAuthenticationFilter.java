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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.service.DataAccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 * @author Manda Wilson
 */
@Component
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private DataAccessTokenService tokenService;

    private static final String BEARER = "Bearer";

    private static final Log LOG = LogFactory.getLog(TokenAuthenticationFilter.class);

    public TokenAuthenticationFilter() {
        // allow any request to contain an authorization header
        super("/**");
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
    public Authentication attemptAuthentication (
        final HttpServletRequest request,
        final HttpServletResponse response) {

        String token = extractHeaderToken(request);

        if (token == null) {
            LOG.error("No token was found in request header.");
            throw new BadCredentialsException("No token was found in request header.");
        }

        Authentication auth = tokenService.createAuthenticationRequest(token);

        return getAuthenticationManager().authenticate(auth);
    }

    @Override
    protected void successfulAuthentication (
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain,
        final Authentication authResult) throws IOException, ServletException {
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
        if (!StringUtils.isEmpty(authorizationHeader)) {
            if ((authorizationHeader.toLowerCase().startsWith(BEARER.toLowerCase()))) {
                return authorizationHeader.substring(BEARER.length()).trim();
            }
        }

        return null;
    }
}

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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

/**
 *
 * @author Manda Wilson
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER = "Bearer";

    private static final Log log = LogFactory.getLog(TokenAuthenticationFilter.class);

    public TokenAuthenticationFilter() {
        // allow any request to contain an authorization header
        super("/**");
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // only required if we do see an authorization header
        String param = request.getHeader(AUTHORIZATION);
        if (param == null) {
            log.debug("attemptAuthentication(), authorization header is null, continue on to other security filters");
            return false;
        }
        return true;
    }

    @Override
    public Authentication attemptAuthentication (
        final HttpServletRequest request,
        final HttpServletResponse response) {

        String param = request.getHeader(AUTHORIZATION);

        log.debug("attemptAuthentication(), header - " + param);

        // TODO strip out token
        //String token = param;
        //Authentication auth = new UsernamePasswordAuthenticationToken(token, token);

        // when DaoAuthenticationProvider does authentication on user returned by PortalUserDetailsService
        // which has password "unused", this password won't match, and then there is a BadCredentials exception thrown
        // this is a good way to catch that the wrong authetication provider is being used
        Authentication auth = new UsernamePasswordAuthenticationToken("fakeuser@mskcc.org", "does not match unused");
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
}

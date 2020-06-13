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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OAuth2TokenAuthenticationProvider implements AuthenticationProvider {

    @Value("${dat.oauth2.jwtRolesPath:resource_access::cbioportal::roles}")
    private String jwtRolesPath;

    @Autowired
    OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(OAuth2BearerAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String offlineToken = (String) ((OAuth2BearerAuthenticationToken) authentication).getCredentials();

        // Note: validity of the offline token is not checked in cBioPortal
        // backend, is handeled by the OAuth2 authentication server.

        // request an access token from the OAuth2 identity provider
        final String accessToken = tokenRefreshRestTemplate.getAccessToken(offlineToken);

        Set<GrantedAuthority> authorities = extractAuthorities(accessToken);

        return new OAuth2BearerAuthenticationToken(authentication.getPrincipal(), authorities);
    }

    // Read roles/authorities from JWT token.
    private Set<GrantedAuthority> extractAuthorities(final String token) throws BadCredentialsException {
        try {
            final Jwt tokenDecoded = JwtHelper.decode(token);
            final String claims = tokenDecoded.getClaims();
            JsonNode claimsMap = new ObjectMapper().readTree(claims);
            JsonNode rolesArrayCursor = claimsMap;
            for (String keyName: jwtRolesPath.split("::")) {
                if (rolesArrayCursor.has(keyName)) {
                    rolesArrayCursor = rolesArrayCursor.get(keyName);
                } else {
                    throw new BadCredentialsException("Cannot find user roles in JWT access token with path '"
                        + jwtRolesPath + "''. Please ensure the dat.oauth2.jwtRolesPath property is correct.");
                }
            }
            final JsonNode rolesArrayPointer = rolesArrayCursor;
            final Iterable<JsonNode> roles = () -> rolesArrayPointer.getElements();

            return StreamSupport.stream(roles.spliterator(), false)
                .map(role -> role.toString().replaceAll("\"", ""))
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toSet());

        } catch (Exception e) {
            throw new BadCredentialsException("Authorities could not be extracted from access token.");
        }
    }

}

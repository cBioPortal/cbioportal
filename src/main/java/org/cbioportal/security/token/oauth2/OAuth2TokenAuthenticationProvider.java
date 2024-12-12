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

package org.cbioportal.security.token.oauth2;

import java.util.Map;
import java.util.Collection;
import java.io.IOException;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.JWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuth2TokenAuthenticationProvider implements AuthenticationProvider {

    @Value("${dat.oauth2.jwtRolesPath:resource_access::cbioportal::roles}")
    private String jwtRolesPath;

    private final OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2TokenAuthenticationProvider.class);

    public OAuth2TokenAuthenticationProvider(OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate) {
        this.tokenRefreshRestTemplate = tokenRefreshRestTemplate;
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(OAuth2BearerAuthenticationToken.class);
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String offlineToken = (String) authentication.getCredentials();

        // Note: validity of the offline token is not checked in cBioPortal
        // backend, is handeled by the OAuth2 authentication server.

        // request an access token from the OAuth2 identity provider
        final String accessToken = tokenRefreshRestTemplate.getAccessToken(offlineToken);

        Collection<GrantedAuthority> authorities = extractAuthorities(accessToken);
        String username = getUsername(accessToken);
        return new OAuth2BearerAuthenticationToken(username, authorities);
    }

    // Read roles/authorities from JWT token.
    private Collection<GrantedAuthority> extractAuthorities(final String token) throws BadCredentialsException {
        try {
            final JWT tokenDecoded = JWTParser.parse(token);
            final Map<String, Object> claims = tokenDecoded.getJWTClaimsSet().toJSONObject();
            return GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(ClaimRoleExtractorUtil.extractClientRoles(claims, jwtRolesPath));

        } catch (Exception e) {
            throw new BadCredentialsException("Authorities could not be extracted from access token: " + e.getMessage());
        }
    }

    private String getUsername(final String token) {

        try {
            final JWT tokenDecoded = JWTParser.parse(token);
            final Object sub = tokenDecoded.getJWTClaimsSet().getClaim("sub");
            return (sub instanceof String) ? (String) sub : null;
        } catch (Exception e) {
            LOG.warn("Failed to parse token: authentiaction failed!", e);
            return null;
        }
    }

}

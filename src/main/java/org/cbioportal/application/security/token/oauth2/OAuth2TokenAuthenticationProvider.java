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

package org.cbioportal.application.security.token.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Collection;
import org.cbioportal.application.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.application.security.util.GrantedAuthorityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class OAuth2TokenAuthenticationProvider implements AuthenticationProvider {

  private static final Logger LOG =
      LoggerFactory.getLogger(OAuth2TokenAuthenticationProvider.class);

  @Value("${dat.oauth2.jwtRolesPath:resource_access::cbioportal::roles}")
  private String jwtRolesPath;

  private final OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;

  public OAuth2TokenAuthenticationProvider(
      OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate) {
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

    try {
      SignedJWT signedJWT = SignedJWT.parse(accessToken);
      JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

      String username = claimsSet.getSubject();
      if (username == null) {
        throw new BadCredentialsException("Username (sub claim) not found in access token.");
      }

      Collection<GrantedAuthority> authorities = extractAuthorities(claimsSet);
      return new OAuth2BearerAuthenticationToken(username, authorities);

    } catch (ParseException e) {
      LOG.warn("Access token parsing failed: {}", e.getMessage());
      throw new BadCredentialsException("Invalid access token: " + e.getMessage(), e);
    }
  }

  // Read roles/authorities from JWT token.
  private Collection<GrantedAuthority> extractAuthorities(final JWTClaimsSet claimsSet)
      throws BadCredentialsException {
    try {
      // ClaimRoleExtractorUtil expects a JSON string representation of the claims
      String claimsJson = claimsSet.toJSONObject().toJSONString();
      return GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(
          ClaimRoleExtractorUtil.extractClientRoles(claimsJson, jwtRolesPath));

    } catch (Exception e) {
      // Catching a broader exception here as ClaimRoleExtractorUtil might throw various things
      // if the claims structure is unexpected.
      LOG.warn("Authorities extraction failed: {}", e.getMessage());
      throw new BadCredentialsException(
          "Authorities could not be extracted from access token.", e);
    }
  }
}

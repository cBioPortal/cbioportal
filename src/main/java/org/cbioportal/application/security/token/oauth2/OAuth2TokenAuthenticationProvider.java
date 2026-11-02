/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.token.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import org.cbioportal.application.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.application.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

public class OAuth2TokenAuthenticationProvider implements AuthenticationProvider {

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

    Collection<GrantedAuthority> authorities = extractAuthorities(accessToken);
    String username = getUsername(accessToken);

    return new OAuth2BearerAuthenticationToken(username, authorities);
  }

  // Read roles/authorities from JWT token.
  private Collection<GrantedAuthority> extractAuthorities(final String token)
      throws BadCredentialsException {
    try {
      final Jwt tokenDecoded = JwtHelper.decode(token);
      final String claims = tokenDecoded.getClaims();
      return GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(
          ClaimRoleExtractorUtil.extractClientRoles(claims, jwtRolesPath));

    } catch (Exception e) {
      throw new BadCredentialsException("Authorities could not be extracted from access token.");
    }
  }

  private String getUsername(final String token) {

    final Jwt tokenDecoded = JwtHelper.decode(token);

    final String claims = tokenDecoded.getClaims();
    JsonNode claimsMap;
    try {
      claimsMap = new ObjectMapper().readTree(claims);
    } catch (IOException e) {
      throw new BadCredentialsException("User name could not be found in access token.");
    }

    if (!claimsMap.has("sub")) {
      throw new BadCredentialsException("User name could not be found in access token.");
    }

    return claimsMap.get("sub").asText();
  }
}

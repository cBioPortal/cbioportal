/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.token.oauth2;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/** OAuth2BearerAuthenticationToken */
public class OAuth2BearerAuthenticationToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = 1L;
  private final String accessToken;
  private final Object principal;

  public OAuth2BearerAuthenticationToken(String accessToken) {
    super(new HashSet<>());
    this.principal = null;
    this.accessToken = accessToken;
  }

  public OAuth2BearerAuthenticationToken(Object principal, String accessToken) {
    super(new HashSet<>());
    this.accessToken = accessToken;
    this.principal = principal;
  }

  public OAuth2BearerAuthenticationToken(
      Object principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    accessToken = null;
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return accessToken;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}

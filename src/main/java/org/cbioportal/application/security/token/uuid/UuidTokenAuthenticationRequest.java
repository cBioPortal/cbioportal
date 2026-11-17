package org.cbioportal.application.security.token.uuid;

import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Username resolved from a valid UUID data access token, to be authenticated by {@link
 * UuidTokenAuthenticationProvider}. A dedicated type keeps that provider from accepting
 * username/password logins, whose credentials it does not check.
 */
public class UuidTokenAuthenticationRequest extends AbstractAuthenticationToken {

  private static final long serialVersionUID = 1L;
  private final String username;

  public UuidTokenAuthenticationRequest(String username) {
    super(List.of());
    this.username = username;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return username;
  }
}

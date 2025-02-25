package org.cbioportal.application.security.util;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class GrantedAuthorityUtil {
  private static final String PREFIX_RESOURCE_ROLE = "ROLE_";

  public static Collection<GrantedAuthority> generateGrantedAuthoritiesFromRoles(
      Collection<String> roles) {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + role))
        .collect(Collectors.toSet());
  }
}

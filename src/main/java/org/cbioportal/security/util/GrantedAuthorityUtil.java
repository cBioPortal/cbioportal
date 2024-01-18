package org.cbioportal.security.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class GrantedAuthorityUtil {
    private static final String PREFIX_RESOURCE_ROLE = "ROLE_";
    public static Collection<GrantedAuthority> generateGrantedAuthoritiesFromRoles(Collection<String> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + role)).collect(Collectors.toSet());
    }
}

package org.cbioportal.security;

import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.cbioportal.security.util.ClaimRoleExtractorUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Objects;

/**
 * Custom JWT GrantedAuthorities Converter to extract roles from JWT token.
 * Claims should equal resource_access:clientId:roles
 */
public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String DEFAULT_CLIENT_ID = "cbioportal";
    
    private  String jwtRolePathClientId;
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(getAuthorities(jwt));
    }

    private Collection<String> getAuthorities(Jwt jwt) {
        return ClaimRoleExtractorUtil.extractClientRoles(this.getJwtRolePathClientId(), jwt.getClaims());
    }
    
    private String getJwtRolePathClientId() {
        return Objects.isNull(jwtRolePathClientId) ? DEFAULT_CLIENT_ID : jwtRolePathClientId;
    }
    
    public void setClientId(String clientId) {
        this.jwtRolePathClientId = clientId;
    }
}

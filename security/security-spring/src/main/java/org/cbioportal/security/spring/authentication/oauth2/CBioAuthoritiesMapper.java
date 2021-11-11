package org.cbioportal.security.spring.authentication.oauth2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

public class CBioAuthoritiesMapper implements GrantedAuthoritiesMapper {

//    @Value("${}")

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
        Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            if (OidcUserAuthority.class.isInstance(authority)) {
                OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

                OidcIdToken idToken = oidcUserAuthority.getIdToken();
                OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                // Map the claims found in idToken and/or userInfo
                // to one or more GrantedAuthority's and add it to mappedAuthorities
                int i = 0;

            } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;

                Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                // Map the attributes found in userAttributes
                // to one or more GrantedAuthority's and add it to mappedAuthorities
                int i = 0;

            }
        });

        return mappedAuthorities;
    }
}
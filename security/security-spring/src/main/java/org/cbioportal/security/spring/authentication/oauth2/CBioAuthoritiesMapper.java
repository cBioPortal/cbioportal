package org.cbioportal.security.spring.authentication.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.Assert;

// TODO add tests
public class CBioAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final Log logger = LogFactory.getLog(getClass());

    private String jwtRolesPath;

    public CBioAuthoritiesMapper(String jwtRolesPath) {
        this.jwtRolesPath = jwtRolesPath;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
        Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        Assert.notNull(authorities, "Authorities cannot be null.");
        Optional<? extends GrantedAuthority> firstAuthority = authorities.stream().findFirst();
        if (!firstAuthority.isPresent()) {
            return authorities;
        }

        if (firstAuthority.get() instanceof OidcUserAuthority) {
            OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) firstAuthority.get();
            OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
            mappedAuthorities = new HashSet<>(getAuthoritiesFromPath(userInfo.getClaims()));
        } else if (firstAuthority.get() instanceof OAuth2UserAuthority) {
            OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) firstAuthority.get();
            Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
            mappedAuthorities = new HashSet<>(getAuthoritiesFromPath(userAttributes));
        }

        return mappedAuthorities;
    }

    private List<GrantedAuthority> getAuthoritiesFromPath(Map<String, Object> userAttributes) {
        Assert.notNull(userAttributes,
            "userAttributes passed in for attribute mapping is null.");
        Object cursor = userAttributes;
        for (String keyName : jwtRolesPath.split("::")) {
            if (cursor instanceof Map && ((Map) cursor).containsKey(keyName)) {
                cursor = ((Map) cursor).get(keyName);
            } else {
                logger.warn(
                    "Bad path! No userAttribute found for path element '" + keyName + "'");
                throw new BadCredentialsException(
                    "Cannot find user roles in userAttributes for element '" + keyName +
                        "' of path '" + jwtRolesPath +
                        "''. Please ensure the dat.oauth2.jwtRolesPath property is correct.");
            }
        }
        if (cursor instanceof List) {
            String[] authoritiesFromUserInfo = ((List<String>) cursor).toArray(String[]::new);
            return AuthorityUtils.createAuthorityList(authoritiesFromUserInfo);
        } else {
            logger.warn(
                "No list of user roles found in user info token value at path! Returning empty list.");
            return Collections.emptyList();
        }
    }

}
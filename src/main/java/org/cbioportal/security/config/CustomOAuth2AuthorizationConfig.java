package org.cbioportal.security.config;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(value = "authorization", havingValue = "true")
public class CustomOAuth2AuthorizationConfig {
    Logger log = LoggerFactory.getLogger(CustomOAuth2AuthorizationConfig.class);

    private final SecurityRepository securityRepository;

    private static final String NAME_ATTRIBUTE_KEY = "email";

    @Autowired
    public CustomOAuth2AuthorizationConfig(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            log.debug("Custom OAuth2 Authorization Enabled");

            // Delegate to the default implementation for loading a user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            var authenticatedPortalUser = loadPortalUser(oidcUser.getEmail());
            if (Objects.isNull(authenticatedPortalUser.cbioUser) || !authenticatedPortalUser.cbioUser.isEnabled()) {
                log.debug("User: {} either not in db or not authorized", oidcUser.getEmail());
                throw new OAuth2AuthenticationException("user not authorized");
            }
            Set<GrantedAuthority> mappedAuthorities = authenticatedPortalUser.authorities;
            oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), NAME_ATTRIBUTE_KEY);
            return oidcUser;
        };
    }

    private AuthenticatedPortalUser loadPortalUser(String email) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        User cbioUser = securityRepository.getPortalUser(email);
        if (!Objects.isNull(cbioUser)) {
            UserAuthorities authorities = securityRepository.getPortalUserAuthorities(email);
            if (!Objects.isNull(authorities)) {
                mappedAuthorities.addAll(AuthorityUtils.createAuthorityList(authorities.getAuthorities()));
            }
        }
        return new AuthenticatedPortalUser(cbioUser, mappedAuthorities);
    }

    record AuthenticatedPortalUser(User cbioUser, Set<GrantedAuthority> authorities) {

    }

}
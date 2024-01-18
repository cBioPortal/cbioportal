package org.cbioportal.security.config;

import org.cbioportal.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2SecurityConfig {
    
    @Value("${spring.security.oauth2.client.jwt-roles-path:resource_access::cbioportal::roles}")
    private String jwtRolesPath;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // FIXME - csrf should be enabled
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> 
                authorize
                    .requestMatchers("/api/health", "/login", "/images/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(login ->
                login
                    // TODO: Add constants
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo ->
                    userInfo.userAuthoritiesMapper(userAuthoritiesMapper())
                )
                    .failureUrl("/login?logout_failure")
            )
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
            );
        return http.build();
    }
    

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                List<Map<String, Object>> claims = new ArrayList<>();
                if (authority instanceof OidcUserAuthority oidcUserAuthority && !Objects.isNull(oidcUserAuthority.getUserInfo())) {
                    claims.add(oidcUserAuthority.getUserInfo().getClaims());
                    claims.add(oidcUserAuthority.getIdToken().getClaims());
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    claims.add(oauth2UserAuthority.getAttributes());
                }
                if(!claims.isEmpty()) {
                    var roles = claims.stream()
                        .filter(claim -> !Objects.isNull(claim))
                        .map(claim -> ClaimRoleExtractorUtil.extractClientRoles(claim, jwtRolesPath))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
                    
                    mappedAuthorities.addAll(GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(roles));
                }
                });
            return mappedAuthorities;
        };
    }

    // See: https://docs.spring.io/spring-security/reference/5.7-SNAPSHOT/servlet/oauth2/login/advanced.html#oauth2login-advanced-oidc-logout
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

}

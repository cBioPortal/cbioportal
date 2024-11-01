package org.cbioportal.security.config;

import org.cbioportal.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private static final Logger log = LoggerFactory.getLogger(OAuth2SecurityConfig.class);


    @Value("${spring.security.oauth2.client.jwt-roles-path:resource_access::cbioportal::roles}")
    private String jwtRolesPath;
    
    private static final String LOGIN_URL = "/login";

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> 
                authorize
                    .requestMatchers("/api/health", LOGIN_URL, "/images/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(login ->
                login
                    .loginPage(LOGIN_URL)
                    .userInfoEndpoint(userInfo ->
                    userInfo.userAuthoritiesMapper(userAuthoritiesMapper())
                )
                    .failureUrl(LOGIN_URL + "?logout_failure")
            )
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
            );
        return http.build();
    }
    

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                List<Map<String, Object>> claims = new ArrayList<>();
                switch (authority) {
                    case OidcUserAuthority oidcUserAuthority -> {
                        if(!Objects.isNull(oidcUserAuthority.getUserInfo())) {
                            claims.add(oidcUserAuthority.getUserInfo().getClaims());
                        }
                        claims.add(oidcUserAuthority.getIdToken().getClaims());
                    }
                    case OAuth2UserAuthority oAuth2UserAuthority -> claims.add(oAuth2UserAuthority.getAttributes());
                    case SimpleGrantedAuthority simpleGrantedAuthority -> mappedAuthorities.add(simpleGrantedAuthority);
                    default -> log.debug("Unsupported UserAuthority Type {}", authority);
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
    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

}

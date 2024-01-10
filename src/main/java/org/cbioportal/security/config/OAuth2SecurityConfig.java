package org.cbioportal.security.config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
public class OAuth2SecurityConfig {
    
    // TODO - add this to portal.properties.EXAMPLE
    // TODO - discuss changing this to /logout (Spring Security default) with Aaron
    @Value("${oauth2.logout.url:/j_spring_security_logout}")
    private String logoutUrl;
    
    // FIXME - we cannot rely on the idp being named 'keycloak' here. In principle
    // it can be any name, and there can be multiple idp's configured.
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id:cbioportal_oauth2}")
    private String clientId;

    // TODO - implement configured retrieval of roles based on this property
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
                login.userInfoEndpoint(userInfo ->
                    userInfo.userAuthoritiesMapper(userAuthoritiesMapper())
                )
            )
            .logout(logout -> logout
                .logoutUrl(logoutUrl)
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
            );
        return http.build();
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OidcUserAuthority.class.isInstance(authority)) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                    getRolesFromIdToken(idToken.getClaims()).forEach(role -> {
                        mappedAuthorities.add(role);
                    });
                    getRolesFromUserInfo(userInfo.getClaims()).forEach(role -> {
                        mappedAuthorities.add(role);
                    });
                    // This section is not needed for the Keycloak OIDC client, but is included for interoperability with pure OAuth2 providers
                } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                    getRolesFromIdToken(userAttributes).forEach(role -> {
                        mappedAuthorities.add(role);
                    });

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

    private List<GrantedAuthority> getRolesFromUserInfo(Map<String, Object> claims) {
        if (claims.containsKey("resource_access")) {
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            if (resourceAccess.containsKey(clientId)) {
                Map<String, Object> cbioportalClient = (Map<String, Object>) resourceAccess.get(clientId);
                if (cbioportalClient.containsKey("roles")) {
                    List<String> roles = (List<String>) cbioportalClient.get("roles");
                    return roles.stream()
                        .map(r -> new SimpleGrantedAuthority(r))
                        .collect(Collectors.toList());
                }
            }
        }
        return List.of();
    }


    private List<GrantedAuthority> getRolesFromIdToken(Map<String, Object> claims) {
        if (claims.containsKey("resource_access")) {
            JSONObject resourceAccess = (JSONObject) claims.get("resource_access");
            if (resourceAccess.containsKey(clientId)) {
                JSONObject cbioportalClient = (JSONObject) resourceAccess.get(clientId);
                if (cbioportalClient.containsKey("roles")) {
                    JSONArray roles = (JSONArray) cbioportalClient.get("roles");
                    return (List<GrantedAuthority>) roles.stream()
                        .map(r -> new SimpleGrantedAuthority((String) r))
                        .collect(Collectors.toList());
                }
            }
        }
        return List.of();
    }

}

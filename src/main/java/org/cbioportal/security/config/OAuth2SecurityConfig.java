package org.cbioportal.security.config;

import org.cbioportal.security.util.OidcRoleExtractorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
public class OAuth2SecurityConfig {

    private static final String PREFIX_RESOURCE_ROLE = "ROLE_";
    
    @Value("${spring.security.oauth2.roles-path.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwtResourceServerUri;
    
    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
    public SecurityFilterChain oAuth2filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable);
        
        if(!Objects.isNull(this.jwtResourceServerUri) && !this.jwtResourceServerUri.isEmpty()) {
            http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        }
        return http.build();
    }
    
    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "optional_oauth2")
    public SecurityFilterChain optionalOAuth2filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2Login(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll()
                .anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {

                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
                   
                    var roles = OidcRoleExtractorUtil.extractClientRoles(this.clientId, userInfo);
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));

                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                }
            });

            return mappedAuthorities;
        };
    }

    Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + role)).collect(Collectors.toList());
    }
    
    
}

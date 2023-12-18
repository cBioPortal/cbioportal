package org.cbioportal.security.config;

import org.cbioportal.security.CustomJwtGrantedAuthoritiesConverter;
import org.cbioportal.security.util.ClaimRoleExtractorUtil;
import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Configuration
@EnableWebSecurity
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
public class OAuth2SecurityConfig {

    
    @Value("${spring.security.oauth2.roles-path.client-id:}")
    private String clientId;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String jwtResourceServerUri;
    
    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "oauth2")
    public SecurityFilterChain oAuth2filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/health", "/login", "/images/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2Login(oauth -> oauth.loginPage("/login"))
            .logout((logout) -> logout.logoutSuccessUrl("/login?logout_success"))
            .exceptionHandling(eh ->
                eh.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AntPathRequestMatcher.antMatcher("/api/**")))
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
            .oauth2Login(oauth -> oauth.loginPage("/login"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll()
                .anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .logout((logout) -> logout.logoutSuccessUrl("/"))
            .build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                Map<String, Object> claims = null;
                if (authority instanceof OidcUserAuthority oidcUserAuthority && !Objects.isNull(oidcUserAuthority.getUserInfo())) {
                        claims = oidcUserAuthority.getUserInfo().getClaims();
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    claims = oauth2UserAuthority.getAttributes();
                }
                if(!Objects.isNull(claims)) {
                    var roles = ClaimRoleExtractorUtil.extractClientRoles(this.clientId, claims);
                    mappedAuthorities.addAll(GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(roles)); 
                }
            });

            return mappedAuthorities;
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        CustomJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new CustomJwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setClientId(this.clientId);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}

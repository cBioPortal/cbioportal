package org.cbioportal.security.config;

import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class Saml2SecurityConfig {
    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "saml")
    public SecurityFilterChain samlFilterChain(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(rolesConverter());
        
        return http.authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/health", "/login", "/images/**" ).permitAll()
                    .anyRequest().authenticated())
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(authenticationProvider)))
            .logout(logout -> logout.logoutSuccessUrl("/login?logout_success"))
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> rolesConverter() {

        Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return (responseToken) -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            var principal = (Saml2AuthenticatedPrincipal) Objects.requireNonNull(authentication).getPrincipal();
            Collection<String> roles = principal.getAttribute("Role");
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            if (!Objects.isNull(roles)) {
                mappedAuthorities.addAll(GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(roles)); 
            } else {
                mappedAuthorities.addAll(authentication.getAuthorities());
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), mappedAuthorities);
        };
    }
}

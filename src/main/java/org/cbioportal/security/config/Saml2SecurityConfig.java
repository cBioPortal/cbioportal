package org.cbioportal.security.config;

import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@ConditionalOnExpression("{'saml','saml_plus_basic'}.contains('${authenticate}')")
public class Saml2SecurityConfig {
   
    private static final String LOGOUT_URL = "/logout";
    
    @Value("${saml.idp.metadata.attribute.role:Role}")
    private String roleAttributeName;

    @Bean
    @ConditionalOnProperty(value = "authenticate", havingValue = "saml")
    public SecurityFilterChain samlFilterChain(HttpSecurity http, RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/health", "/images/**", "/js/**", "/login").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(eh ->
                eh.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AntPathRequestMatcher.antMatcher("/api/**")
                )
            )
            .saml2Login(withDefaults())
            // NOTE: I did not get the official .saml2Logout() DSL to work as
            // described at https://docs.spring.io/spring-security/reference/6.1/servlet/saml2/logout.html
            // Logout Service POST Binding URL: http://localhost:8080/logout/saml2/slo
            .logout(logout -> logout
                .logoutUrl(LOGOUT_URL)
                .logoutSuccessHandler(logoutSuccessHandler(relyingPartyRegistrationRepository))
            )
            .build();
    }
    
    @Bean
    public OpenSaml4AuthenticationProvider openSaml4AuthenticationProvider() {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(rolesConverter());
        return authenticationProvider;
    }
    
    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> rolesConverter() {

        Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return (responseToken) -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            var principal = (Saml2AuthenticatedPrincipal) Objects.requireNonNull(authentication).getPrincipal();
            Collection<String> roles = principal.getAttribute(this.roleAttributeName);
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            if (!Objects.isNull(roles)) {
                mappedAuthorities.addAll(GrantedAuthorityUtil.generateGrantedAuthoritiesFromRoles(roles)); 
            } else {
                mappedAuthorities.addAll(authentication.getAuthorities());
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), mappedAuthorities);
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        // Perform logout at the SAML2 IDP
        DefaultRelyingPartyRegistrationResolver relyingPartyRegistrationResolver =
            new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);
        OpenSaml4LogoutRequestResolver logoutRequestResolver =
            new OpenSaml4LogoutRequestResolver(relyingPartyRegistrationResolver);

        return new Saml2RelyingPartyInitiatedLogoutSuccessHandler(logoutRequestResolver);
    }
    
}

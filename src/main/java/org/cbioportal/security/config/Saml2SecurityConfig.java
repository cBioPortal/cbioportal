package org.cbioportal.security.config;

import org.cbioportal.security.util.GrantedAuthorityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
import org.springframework.security.web.DefaultSecurityFilterChain;
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
@ConditionalOnProperty(value = "authenticate", havingValue = "saml")
public class Saml2SecurityConfig {
    
    @Value("${saml.logout.url:/saml/logout}")
    private String logoutUrl;

    @Autowired(required = false)
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    @Autowired
    public void configure(AuthenticationManagerBuilder builder) {
        OpenSaml4AuthenticationProvider saml4AuthenticationProvider = new OpenSaml4AuthenticationProvider();
        saml4AuthenticationProvider.setResponseAuthenticationConverter(rolesConverter());
        builder.authenticationProvider(saml4AuthenticationProvider);
    }
    
    @Bean
    public SecurityFilterChain samlFilterChain(HttpSecurity http) throws Exception {
        DefaultSecurityFilterChain build = http
            // FIXME - csrf should be enabled
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/health", "/images/**", "/js/**").permitAll()
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
                .logoutUrl(logoutUrl)
                .logoutSuccessHandler(logoutSuccessHandler())
            )
            .build();
        return build;
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

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        // Perform logout at the SAML2 IDP
        DefaultRelyingPartyRegistrationResolver relyingPartyRegistrationResolver =
            new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);
        OpenSaml4LogoutRequestResolver logoutRequestResolver =
            new OpenSaml4LogoutRequestResolver(relyingPartyRegistrationResolver);
        return new Saml2RelyingPartyInitiatedLogoutSuccessHandler(logoutRequestResolver);
    }
    
}

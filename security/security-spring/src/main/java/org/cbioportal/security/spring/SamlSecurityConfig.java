package org.cbioportal.security.spring;

import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.cbioportal.security.spring.authentication.PortalUserDetailsService;
import org.cbioportal.security.spring.authentication.saml.SamlResponseAuthenticationConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "saml")
public class SamlSecurityConfig extends WebSecurityConfigurerAdapter {

    // TODO add to docs
    // Autoconfig requires registering singing certificate and private key. Follow these steps:
    // 1. openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
    // 2. Save/mount in classpath
    // 3. Import cert.pem in Keycloak (client->keys->import->certificate PEM)
    // 4. Add properties to portal.properties:
    //    - spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].certificate-location=classpath:/cert.pem
    //    - spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].private-key-location=classpath:/key.pem

    // This is the URL called by the frontend to initiate logout
    @Value("${saml.logout.url:/saml/logout}")
    private String logoutUrl;
    
    @Autowired
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
            .saml2Login()
                .authenticationManager(new ProviderManager(authenticationProvider()))
                .successHandler(authenticationSuccessHandler())
            .and()
            // NOTE: I did not get the official .saml2Logout() DSL to work.
            // TODO add docs Keyclook saml2 client configured with:
            //  BaseURL: http://localhost:8080
            //  Logout Service POST Binding URL: http://localhost:8080/logout/saml2/slo
            .logout(logout -> logout
                .logoutUrl(logoutUrl)
                .logoutSuccessUrl("/")       // triggering this backend url will terminate the user session
                .logoutSuccessHandler(
                    logoutSuccessHandler()  // when successful this handler will trigger SSO logout
                )
                .deleteCookies("JSESSIONID"));
    }

    @Bean
    public PortalUserDetailsService portalUserDetailsService() {
        return new PortalUserDetailsService();
    }

    @Bean
    public SamlResponseAuthenticationConverter responseAuthenticationConverter() {
        return new SamlResponseAuthenticationConverter(portalUserDetailsService());
    }
    
    @Bean
    public PortalSavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new PortalSavedRequestAwareAuthenticationSuccessHandler();
    }

    @Bean
    public OpenSaml4AuthenticationProvider authenticationProvider() {
        OpenSaml4AuthenticationProvider authenticationProvider =
            new OpenSaml4AuthenticationProvider();
        // Set the converter that converts role attributes to granted authorities.
        authenticationProvider.setResponseAuthenticationConverter(
            responseAuthenticationConverter());
        return authenticationProvider;
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

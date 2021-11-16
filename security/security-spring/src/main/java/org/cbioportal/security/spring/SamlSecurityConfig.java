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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
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

    private static Logger log = LoggerFactory.getLogger(SamlSecurityConfig.class);

    public static final String SAML_IDP_REGISTRATION_ID = "cbioportal_saml_idp";

    @Value("${saml.logout.url:/saml/logout}")
    private String logoutUrl;

    // Keystore
    @Value("${saml.keystore.location}")
    private Resource samlKeystoreLocation;
    @Value("${saml.keystore.password}")
    private String samlKeystorePassword;
    @Value("${saml.keystore.private-key.key}")
    private String samlPrivateKeyKey;
    @Value("${saml.keystore.private-key.password}")
    private String samlPrivateKeyPassword;
    @Value("${saml.keystore.default-key}")
    private String samlKeystoreDefaultKey;

    // SP
    @Value("${saml.sp.metadata.entityid}")
    private String samlSPMetadataEntityId;
    // TODO what to do with these properties>?
    @Value("${saml.sp.metadata.entityBaseURL:#{null}}")
    private URL samlSPMetadataEntityBaseUrl;
    @Value("${saml.sp.metadata.wantassertionssigned:true}")
    private boolean samlSPMetadataWantAssertionsSigned;

    // IDP
    @Value("${saml.idp.metadata.location}")
    private String samlIdpMetadataLocation;
    @Value("${saml.idp.comm.binding.settings:defaultBinding}")
    private String samlIdpBindingSetting; // should be 'defaultBinding' or 'specificBinding'
    @Value("${saml.idp.comm.binding.type}")
    private String samlIdpBindingType;
    
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
            // TODO fix logout redirect. Logout works, but the browser makes a final POST to /saml/logout (caused by keycloak Logout POST binding?)
            // This is not accepted by cBio backend 
            .and()
            .logout(logout -> logout
                .logoutUrl(logoutUrl)       // triggering this backend url will terminate the user session
                .logoutSuccessUrl("/")
                .logoutSuccessHandler(
                    logoutSuccessHandler()  // when successful this handler will trigger SSO logout
                )
                .deleteCookies("JSESSIONID"));
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {

        try {
            // Read private key and certificate from java keystore.
            // Dev note: The use of this keystore prevents us from 
            // using Spring Boot autoconfiguration from properties file.
            KeyStore keystore = KeyStore.getInstance("jks");
            keystore.load(samlKeystoreLocation.getInputStream(),
                samlKeystorePassword.toCharArray());
            PrivateKey privateKey =
                (PrivateKey) keystore.getKey(samlPrivateKeyKey,
                    samlPrivateKeyPassword.toCharArray());
            X509Certificate cert =
                (X509Certificate) keystore.getCertificate(samlKeystoreDefaultKey);

            RelyingPartyRegistration.Builder builder = RelyingPartyRegistrations
                .fromMetadataLocation(samlIdpMetadataLocation)
                .registrationId(SAML_IDP_REGISTRATION_ID)
                .entityId(samlSPMetadataEntityId)
                .signingX509Credentials(
                    c -> c.add(Saml2X509Credential.signing(privateKey, cert))
                );

            // When configured, do not use the defaultBinding provided by the IDP.
            if (samlIdpBindingSetting.equals("specificBinding")) {
                log.debug("Setting binding type to '" + samlIdpBindingType + "'");
                builder.assertionConsumerServiceBinding(
                    Saml2MessageBinding.from(
                        String.format("urn:oasis:names:tc:SAML:2.0:%s", samlIdpBindingType))
                );
            }

            return new InMemoryRelyingPartyRegistrationRepository(builder.build());
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage());
        }

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
        DefaultRelyingPartyRegistrationResolver relyingPartyRegistrationResolver =
            new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository());
        OpenSaml4LogoutRequestResolver logoutRequestResolver =
            new OpenSaml4LogoutRequestResolver(relyingPartyRegistrationResolver);
        return new Saml2RelyingPartyInitiatedLogoutSuccessHandler(logoutRequestResolver);
    }

}

package org.cbioportal.security.spring;

import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

// Conditionally disable autoconfguration for SAML and/or OAuth2.
public class AutoconfigureExcludeConfig {

    @Configuration
    @ConditionalOnProperty(name = "authenticate", havingValue = {"saml", "oauth2"}, isNot = true)
    @EnableAutoConfiguration(exclude={OAuth2ClientAutoConfiguration.class, Saml2RelyingPartyAutoConfiguration.class})
    public static class ExcludeAll {}
    
    @Configuration
    @ConditionalOnProperty(name = "authenticate", havingValue = "saml")
    @EnableAutoConfiguration(exclude=OAuth2ClientAutoConfiguration.class)
    public static class Saml {}


    @Configuration
    @ConditionalOnProperty(name = "authenticate", havingValue = "oauth2")
    @EnableAutoConfiguration(exclude=Saml2RelyingPartyAutoConfiguration.class)
    public static class OAuth2 {}

}

package org.cbioportal.security.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;

@Configuration
@ConditionalOnProperty(value = "authenticate", havingValue = "saml")
public class SamlSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SAMLAuthenticationProvider samlAuthenticationProvider;

    // Add the samlAuthenticationProvider to the AuthenticationManager that 
    // contains the tokenAuthenticationProvider created in AuthenticatedWebSecurityConfig
    // (see: "Customizing Authentication Managers" @ https://spring.io/guides/topicals/spring-security-architecture
    @Override
    public void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(samlAuthenticationProvider);
    }

}

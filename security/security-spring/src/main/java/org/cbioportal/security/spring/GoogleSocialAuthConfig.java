package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.googleplus.GoogleplusConnectionFactory;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.google.security.GoogleAuthenticationService;

@Configuration
@ConditionalOnProperty(name = "authenticate", havingValue = {"social_auth", "social_auth_google"})
public class GoogleSocialAuthConfig {

    @Value("${googleplus.consumer.key}")
    private String consumerKey;

    @Value("${googleplus.consumer.secret}")
    private String consumerSecret;

    @Bean
    public GoogleplusConnectionFactory googleplusConnectionFactory() {
        return new GoogleplusConnectionFactory(consumerKey, consumerSecret);
    }
    
    @Bean
    public GoogleAuthenticationService googleAuthenticationService() {
        GoogleAuthenticationService authenticationService =
            new GoogleAuthenticationService(consumerKey, consumerSecret);
        authenticationService.setConnectionFactory(googleplusConnectionFactory());
        authenticationService.setDefaultScope("email");
        return authenticationService;
    }

}

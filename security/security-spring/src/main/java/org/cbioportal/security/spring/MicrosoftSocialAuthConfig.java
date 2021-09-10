package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.live.LiveConnectionFactory;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.live.security.LiveAuthenticationService;

@Configuration
@ConditionalOnProperty(name = "authenticate", havingValue = {"social_auth_microsoft"})
public class MicrosoftSocialAuthConfig {

    @Value("${microsoftlive.consumer.key}")
    private String consumerKey;

    @Value("${microsoftlive.consumer.secret}")
    private String consumerSecret;

    @Bean
    public LiveConnectionFactory liveConnectionFactory() {
        return new LiveConnectionFactory(consumerKey, consumerSecret);
    }

    @Bean
    public LiveAuthenticationService liveAuthenticationService() {
        LiveAuthenticationService authenticationService =
            new LiveAuthenticationService(consumerKey, consumerSecret);
        authenticationService.setConnectionFactory(liveConnectionFactory());
        authenticationService.setDefaultScope("wl.emails");
        return authenticationService;
    }

}

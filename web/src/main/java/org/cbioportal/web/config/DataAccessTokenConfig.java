package org.cbioportal.web.config;

import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.DataAccessTokenController;
import org.cbioportal.web.OAuth2DataAccessTokenController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource(value="classpath:portal.properties", ignoreResourceNotFound=true),
        @PropertySource(value="file:///${PORTAL_HOME}/portal.properties", ignoreResourceNotFound=true)
})
public class DataAccessTokenConfig {

    // controller
    @Bean
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2")
    public OAuth2DataAccessTokenController oauth2DataAccessTokenController() {
        return new OAuth2DataAccessTokenController();
    }

    @Bean
    @ConditionalOnProperty(name = "dat.method", havingValue = "oauth2", isNot = true)
    public DataAccessTokenController dataAccessTokenController() {
        return new DataAccessTokenController();
    }

}
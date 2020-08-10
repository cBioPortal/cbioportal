package org.cbioportal.web.config;

import org.cbioportal.web.DataAccessTokenController;
import org.cbioportal.web.OAuth2DataAccessTokenController;
import org.cbioportal.web.config.annotation.ConditionalOnDatMethod;
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
    @ConditionalOnDatMethod(value = "oauth2")
    public OAuth2DataAccessTokenController oauth2DataAccessTokenController() {
        return new OAuth2DataAccessTokenController();
    }

    @Bean
    @ConditionalOnDatMethod(value = "oauth2", isNot = true)
    public DataAccessTokenController dataAccessTokenController() {
        return new DataAccessTokenController();
    }

}
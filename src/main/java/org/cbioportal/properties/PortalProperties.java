package org.cbioportal.properties;

import org.cbioportal.utils.validation.AllowedValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties
public class PortalProperties {

    @AllowedValues(values = {"false", "saml", "oauth2",
         "optional_oauth2", "saml_plus_basic"})
    private String authenticate;

    public void setAuthenticate(String authenticate) {
        this.authenticate = authenticate;
    }

    public String getAuthenticate() {
        return authenticate;
    }
}

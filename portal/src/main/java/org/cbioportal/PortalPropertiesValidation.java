package org.cbioportal;

import org.cbioportal.utils.validation.AllowedValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties
public class PortalPropertiesValidation {
    
    @AllowedValues(values = {"false", "noauthsessionservice",
         "googleplus", "openid", "social_auth", "social_auth_google", "social_auth_microsoft"})
    private String authenticate;

    public void setAuthenticate(String authenticate) {
        this.authenticate = authenticate;
    }

    public String getAuthenticate() {
        return authenticate;
    }
}




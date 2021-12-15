package org.cbioportal;

import org.cbioportal.utils.validation.AllowedValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties
public class PortalPropertiesValidation {

    @AllowedValues(values = {"false", "noauthsessionservice", "saml", "oauth2",
        "googleplus", "openid", "ad", "ldap", "social_auth", "social_auth_google",
        "social_auth_microsoft"})
    private String authenticate;

    public void setAuthenticate(String authenticate) {
        this.authenticate = authenticate;
    }

    public String getAuthenticate() {
        return authenticate;
    }
}

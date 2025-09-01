package org.cbioportal.legacy.properties;

import org.cbioportal.legacy.utils.validation.AllowedValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties
public class PortalProperties {

  @AllowedValues(values = {"false", "saml", "oauth2", "optional_oauth2", "saml_plus_basic"})
  private String authenticate;

  public void setAuthenticate(String authenticate) {
    this.authenticate = authenticate;
  }

  public String getAuthenticate() {
    return authenticate;
  }

  @AllowedValues(values = {"false", "gpt", "gemini", "grok", "custom"})
  private String assistant;

  public void setAssistant(String assistant) {
    this.assistant = assistant;
  }

  public String getAssistant() {
    return assistant;
  }
}

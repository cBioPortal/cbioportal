package org.cbioportal.application.security.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Stops startup when {@code authenticate} is not one of the supported modes. */
@Configuration
public class AuthenticationModeConfig {

  static final List<String> MODES =
      List.of("false", "oauth2", "optional_oauth2", "saml", "saml_plus_basic");

  // Compared case-sensitively: the security configurations are selected partly by
  // case-insensitive property conditions and partly by case-sensitive SpEL expressions, so a
  // value in another case enables some of them and not others.
  public AuthenticationModeConfig(@Value("${authenticate:false}") String authenticate) {
    if (!MODES.contains(authenticate)) {
      throw new IllegalStateException(
          "Unsupported value for property 'authenticate': '"
              + authenticate
              + "'. Use one of "
              + MODES
              + " (lower case).");
    }
  }
}

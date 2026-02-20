package org.cbioportal.application.security.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ApiSecurityCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String authenticate = context.getEnvironment().getProperty("authenticate", "false");
    String requireToken = context.getEnvironment().getProperty("dat.require_token", "false");

    boolean isAuthenticationEnabled =
        !"false".equalsIgnoreCase(authenticate)
            && !"optional_oauth2".equalsIgnoreCase(authenticate);
    boolean isTokenRequired = "true".equalsIgnoreCase(requireToken);

    // API Security should be enabled if authentication is globally enabled OR if
    // token is specifically required
    return isAuthenticationEnabled || isTokenRequired;
  }
}

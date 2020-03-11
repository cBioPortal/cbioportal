package org.cbioportal.security.spring.authentication.token.oauth2;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OAuth2DataAccessTokenServiceCondition implements Condition {
    public OAuth2DataAccessTokenServiceCondition() {
        super();
    }
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String method = context.getEnvironment().getProperty("dat.method");
        return method != null && method.equalsIgnoreCase("oauth2");
    }
}

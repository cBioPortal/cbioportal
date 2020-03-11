package org.cbioportal.web;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DataAccessTokenControllerCondition implements Condition {
    public DataAccessTokenControllerCondition() {
        super();
    }
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String method = context.getEnvironment().getProperty("dat.method");
        return method != null && (method.equalsIgnoreCase("uuid") || method.equalsIgnoreCase("jwt"));
    }
}

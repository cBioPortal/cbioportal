package org.cbioportal.service.impl;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class UnauthDataAccessTokenServiceCondition implements Condition {
    public UnauthDataAccessTokenServiceCondition() {
        super();
    }
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String method = context.getEnvironment().getProperty("dat.method");
        return method == null || method == "" || method.equalsIgnoreCase("none");
    }
}

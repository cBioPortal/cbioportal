package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authorization.CancerStudyPermissionEvaluator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnExpression("'${authenticate}' ne 'none'")
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Bean
    public CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator() {
        return new CancerStudyPermissionEvaluator();
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
            new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(cancerStudyPermissionEvaluator());
        return expressionHandler;
    }
}

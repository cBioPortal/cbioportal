package org.cbioportal.security.config;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.security.CancerStudyPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableMethodSecurity
@ConditionalOnExpression("{'oauth2','saml','optional_oauth2'}.contains('${authenticate}')")
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    @Value("${app.name:}")
    private String appName;

    @Value("${filter_groups_by_appname:true}")
    private String doFilterGroupsByAppName;

    @Value("${always_show_study_group:}")
    private String alwaysShowCancerStudyGroup;

    @Qualifier("staticRefCacheMapUtil")
    @Autowired
    private CacheMapUtil cacheMapUtil;
    
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
            new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new CancerStudyPermissionEvaluator(appName, doFilterGroupsByAppName, alwaysShowCancerStudyGroup, cacheMapUtil));
        return expressionHandler;
    }
}
package org.cbioportal.security.config;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.security.CancerStudyPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@ConditionalOnExpression("{'oauth2','saml','optional_oauth2'}.contains('${authenticate}')")
public class MethodSecurityConfig {
    @Value("${app.name:}")
    private static String appName;

    @Value("${filter_groups_by_appname:true}")
    private static String doFilterGroupsByAppName;

    @Value("${always_show_study_group:}")
    private static String alwaysShowCancerStudyGroup;

    @Qualifier("staticRefCacheMapUtil")
    @Autowired
    private static CacheMapUtil cacheMapUtil;
    
    @Bean
    static MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
            new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new CancerStudyPermissionEvaluator(appName, doFilterGroupsByAppName, alwaysShowCancerStudyGroup, cacheMapUtil));
        return expressionHandler;
    }
}
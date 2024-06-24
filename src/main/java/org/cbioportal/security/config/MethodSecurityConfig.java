package org.cbioportal.security.config;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.security.CancerStudyPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
// We are allowing users to enable method_authorization if optional_oauth2 is selected
@ConditionalOnExpression("{'oauth2','saml', 'saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
public class MethodSecurityConfig {
    @Value("${app.name:}")
    private String appName;

    @Value("${filter_groups_by_appname:true}")
    private String doFilterGroupsByAppName;

    @Value("${always_show_study_group:}")
    private String alwaysShowCancerStudyGroup;

    @Autowired
    private CacheMapUtil cacheMapUtil;
    
    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
            new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(cancerStudyPermissionEvaluator());
        return expressionHandler;
    }
    
    @Bean
    public CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator() {
        return new CancerStudyPermissionEvaluator(appName, doFilterGroupsByAppName, alwaysShowCancerStudyGroup, cacheMapUtil);
    }
}
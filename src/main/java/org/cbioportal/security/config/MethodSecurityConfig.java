package org.cbioportal.security.config;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.security.CancerStudyPermissionEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
// We are allowing users to enable method_authorization if optional_oauth2 is selected
@ConditionalOnExpression("{'oauth2','saml', 'saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
public class MethodSecurityConfig {
    
    @Bean
    public CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator(
        @Value("${app.name:}") String appName,
        @Value("${filter_groups_by_appname:true}") String doFilterGroupsByAppName,
        @Value("${always_show_study_group:}") String alwaysShowCancerStudyGroup,
        CacheMapUtil cacheMapUtil
    ) {
        return new CancerStudyPermissionEvaluator(appName, doFilterGroupsByAppName, alwaysShowCancerStudyGroup, cacheMapUtil);
    }

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler(CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler =
            new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(cancerStudyPermissionEvaluator);
        return expressionHandler;
    }
}
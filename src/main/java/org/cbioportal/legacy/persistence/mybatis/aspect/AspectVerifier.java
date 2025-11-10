package org.cbioportal.legacy.persistence.mybatis.aspect;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Verifies that the SqlSessionAspect is properly loaded by Spring
 */
@Component
public class AspectVerifier {

    private static final Logger log = LoggerFactory.getLogger(AspectVerifier.class);

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void verify() {
        try {
            SqlSessionAspect aspect = applicationContext.getBean(SqlSessionAspect.class);
            log.info("✓ SqlSessionAspect bean found: {}", aspect.getClass().getName());
            log.info("✓ @UseSameSqlSession annotation support is active");
        } catch (Exception e) {
            log.error("✗ SqlSessionAspect bean NOT found - aspect will not work!", e);
        }
    }
}
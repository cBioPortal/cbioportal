package org.cbioportal.web.config;

import org.cbioportal.persistence.mybatis.util.CacheMapUtil;
import org.cbioportal.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.cbioportal.web.util.UniqueKeyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public InvolvedCancerStudyExtractorInterceptor involvedCancerStudyExtractorInterceptor() {
        return new InvolvedCancerStudyExtractorInterceptor();
    }

    @MockBean
    private CacheMapUtil cacheMapUtil;

    @MockBean
    private UniqueKeyExtractor uniqueKeyExtractor;

    @Component
    public class InterceptorAppConfig implements WebMvcConfigurer {

        @Autowired
        private HandlerInterceptor involvedCancerStudyExtractorInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(involvedCancerStudyExtractorInterceptor);
        }
    }
}

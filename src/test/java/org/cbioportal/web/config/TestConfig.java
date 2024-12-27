package org.cbioportal.web.config;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.web.error.GlobalExceptionHandler;
import org.cbioportal.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestConfig {

    // -- configure preauthorize security
    @MockBean(name = "staticRefCacheMapUtil")
    private CacheMapUtil cacheMapUtil;

    @Bean
    public InvolvedCancerStudyExtractorInterceptor involvedCancerStudyExtractorInterceptor() {
        return new InvolvedCancerStudyExtractorInterceptor();
    }

    @Component
    public class InterceptorAppConfig implements WebMvcConfigurer {

        @Autowired
        private HandlerInterceptor involvedCancerStudyExtractorInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(involvedCancerStudyExtractorInterceptor)
                .addPathPatterns("/api/**");
        }
    }

    // -- register mixins
    @Bean
    public CustomObjectMapper customObjectMapper() {
        return new CustomObjectMapper();
    }

    // -- handle exceptions
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}

package org.cbioportal.legacy.web.config;

import org.cbioportal.application.rest.error.GlobalExceptionHandler;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestConfig {

  // -- configure preauthorize security. A plain @Bean returning a Mockito mock, rather than
  // @MockitoBean, since none of this test config's many consumers need to stub/verify this mock
  // themselves -- it exists purely to satisfy InvolvedCancerStudyExtractorInterceptor's
  // autowiring in these @WebMvcTest slices, and @MockitoBean fields on a shared
  // @TestConfiguration (rather than the JUnit test class itself) aren't reliably discovered by
  // Spring's bean-override mechanism.
  @Bean
  public CacheMapUtil cacheMapUtil() {
    return Mockito.mock(CacheMapUtil.class);
  }

  @Bean
  public InvolvedCancerStudyExtractorInterceptor involvedCancerStudyExtractorInterceptor() {
    return new InvolvedCancerStudyExtractorInterceptor();
  }

  @Component
  public class InterceptorAppConfig implements WebMvcConfigurer {

    @Autowired private HandlerInterceptor involvedCancerStudyExtractorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(involvedCancerStudyExtractorInterceptor).addPathPatterns("/api/**");
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

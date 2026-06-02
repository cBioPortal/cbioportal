package org.cbioportal.legacy.web.config;

import org.cbioportal.application.rest.error.GlobalExceptionHandler;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

  // -- configure preauthorize security
  @MockBean(name = "staticRefCacheMapUtil")
  private CacheMapUtil cacheMapUtil;

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

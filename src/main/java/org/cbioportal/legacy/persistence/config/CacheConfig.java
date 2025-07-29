package org.cbioportal.legacy.persistence.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
  @Bean
  @ConditionalOnBean(CacheManager.class)
  public CacheManager compositeCacheManager(
      ObjectProvider<CacheManager> allManagers // includes app + all features
      ) {
    List<CacheManager> managers = new ArrayList<>(allManagers.orderedStream().toList());

    CompositeCacheManager composite =
        new CompositeCacheManager(managers.toArray(new CacheManager[0]));
    composite.setFallbackToNoOpCache(false); // fail if cache not found
    return composite;
  }
}

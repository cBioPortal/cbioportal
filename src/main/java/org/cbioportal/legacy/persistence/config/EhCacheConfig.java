package org.cbioportal.legacy.persistence.config;

import org.cbioportal.legacy.persistence.util.CustomEhcachingProvider;
import org.cbioportal.legacy.persistence.util.CustomKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
// Hardcoded to always enable EhCache - no conditional property check
public class EhCacheConfig extends CachingConfigurerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(EhCacheConfig.class);

  @Bean
  public CustomEhcachingProvider customEhcachingProvider() {
    LOG.info("EhCache: Creating CustomEhcachingProvider bean");
    return new CustomEhcachingProvider();
  }

  @Bean
  public javax.cache.CacheManager jCacheManager() {
    LOG.info("EhCache: Initializing JCache CacheManager");
    // Set the JCache provider to EhCache to avoid conflicts with Redisson
    System.setProperty(
        "javax.cache.spi.CachingProvider", "org.ehcache.jsr107.EhcacheCachingProvider");
    javax.cache.CacheManager cacheManager = customEhcachingProvider().getCacheManager();
    if (cacheManager != null) {
      LOG.info("EhCache: JCache CacheManager initialized successfully");
      for (String cacheName : cacheManager.getCacheNames()) {
        LOG.info("EhCache: Registered cache: {}", cacheName);
      }
    } else {
      LOG.error("EhCache: Failed to initialize JCache CacheManager - returned null");
    }
    return cacheManager;
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    LOG.info("EhCache: Creating Spring CacheManager wrapper");
    JCacheCacheManager springCacheManager = new JCacheCacheManager();
    springCacheManager.setCacheManager(jCacheManager());
    LOG.info("EhCache: Spring CacheManager initialized successfully - IN-MEMORY CACHING IS ACTIVE");
    return springCacheManager;
  }

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new CustomKeyGenerator();
  }

  @Bean
  public NamedCacheResolver generalRepositoryCacheResolver() {
    return new NamedCacheResolver(cacheManager(), "GeneralRepositoryCache");
  }

  @Bean
  public NamedCacheResolver staticRepositoryCacheOneResolver() {
    return new NamedCacheResolver(cacheManager(), "StaticRepositoryCacheOne");
  }
}

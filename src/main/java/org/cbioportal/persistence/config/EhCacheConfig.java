package org.cbioportal.persistence.config;

import org.cbioportal.persistence.util.CustomEhcachingProvider;
import org.cbioportal.persistence.util.CustomKeyGenerator;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhCacheConfig extends CachingConfigurerSupport {

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new JCacheCacheManager(
            customEhcachingProvider().getCacheManager()
        );
    }
    
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    @Bean
    public CustomEhcachingProvider customEhcachingProvider() {
        return new CustomEhcachingProvider();
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

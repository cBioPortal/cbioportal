package org.cbioportal.persistence.config;

import org.cbioportal.persistence.util.CustomEhcachingProvider;
import org.cbioportal.persistence.util.CustomKeyGenerator;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Iterator;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhCacheConfig extends CachingConfigurerSupport {

    @Bean
    @Override
    public CacheManager cacheManager() {
        // We somehow end up with both a redisson cache provider and an ehcache provider when using EhCache
        // This just removes the redundant cache provider
        Iterator<CachingProvider> iterator = Caching.getCachingProviders().iterator();
        while(iterator.hasNext()) {
            CachingProvider provider = iterator.next();
            if (!(provider instanceof EhcacheCachingProvider)) {
                iterator.remove();
            }
        }
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

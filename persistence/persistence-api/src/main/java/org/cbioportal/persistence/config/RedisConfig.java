package org.cbioportal.persistence.config;

import org.cbioportal.persistence.util.CustomKeyGenerator;
import org.cbioportal.persistence.util.CustomRedisCachingProvider;
import org.cbioportal.persistence.util.LoggingCacheErrorHandler;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.NamedCacheResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"redis"})
public class RedisConfig extends CachingConfigurerSupport {
    
    @Value("${redis.name:cbioportal}")
    private String redisName;

    @Bean
    @Override
    public CacheManager cacheManager() {
        return customRedisCachingProvider().getCacheManager(
            customRedisCachingProvider().getRedissonClient()
        );
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }
    
    @Bean
    public CustomRedisCachingProvider customRedisCachingProvider() {
        return new CustomRedisCachingProvider();
    }

    @Bean
    public CacheResolver generalRepositoryCacheResolver() {
        return new NamedCacheResolver(cacheManager(), redisName + "GeneralRepositoryCache");
    }
    
    @Bean
    public CacheResolver staticRepositoryCacheOneResolver() {
        return new NamedCacheResolver(cacheManager(), redisName + "StaticRepositoryCacheOne");
    }

}

package org.cbioportal.persistence.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedisCacheUtilsTest {

    @InjectMocks
    private RedisCacheUtils redisCacheUtils;

    @Mock
    private Cache cache;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RKeys keys;

    @Mock
    private CacheManager cacheManager;

    private String cacheName = "1_test_cache";

    @Before
    public void setUp() throws Exception {

        List<String> keysInCache = new ArrayList<>();
        keysInCache.add("1_test_cache:a_study1_a");
        keysInCache.add("1_test_cache:a__a");
        keysInCache.add("2_test_cache:a_study2_a");
        keysInCache.add("2_test_cache:a__a");

        when(cache.getName()).thenReturn(cacheName);
        when(cache.getNativeCache()).thenReturn(redissonClient);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(keys.getKeysStream()).thenReturn(keysInCache.stream());
        when(cacheManager.getCache(eq(cacheName))).thenReturn(cache);
    }
    
    @Test
    public void evictByPatternAll() {
        redisCacheUtils.evictByPattern(cacheName, ".*");
        verify(cache, times(1)).evict(eq(".*"));
    }

    @Test
    public void getKeysSelectsForCacheName() {
        List<String> keys = redisCacheUtils.getKeys(cacheName);
        // make sure the cache removes keys with a different prefix ("2_test_cache" in this case)
        List<String> expectedKeys = Arrays.asList("1_test_cache:a_study1_a", "1_test_cache:a__a");
        Assert.assertArrayEquals(expectedKeys.toArray(), keys.toArray());
    }
}
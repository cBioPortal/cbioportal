package org.cbioportal.persistence.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomRedisCacheManagerTest {
    @Mock
    RedissonClient client;

    @Test
    public void shouldGetStaticCache() {
        CustomRedisCacheManager subject = new CustomRedisCacheManager(client, 100);

        Cache actualFirstCall = subject.getCache("aStaticFunkyCache");
        Cache actualSecondCall = subject.getCache("aStaticFunkyCache");
        
        assertEquals("aStaticFunkyCache", actualFirstCall.getName());
        assertEquals(client, actualFirstCall.getNativeCache());
        // the first and second call should return the same cache object
        assertSame(actualFirstCall, actualSecondCall);
    }

    @Test
    public void shouldReturnNamesOfCachesWhenNonePresent() {
        CustomRedisCacheManager subject = new CustomRedisCacheManager(client, 100);
        Collection<String> actual = subject.getCacheNames();
        assertEquals(new HashSet<>(), new HashSet<>(actual));
    }
    
    @Test
    public void shouldReturnNamesOfCachesWhenSomePresent() {
        CustomRedisCacheManager subject = new CustomRedisCacheManager(client, 100);
        subject.getCache("cache and release", false);
        subject.getCache("Cache-22", true);
        subject.getCache("cache money", false);

        Set<String> actual = new HashSet<>(subject.getCacheNames());
        HashSet<String> expected = 
            new HashSet<>(Arrays.asList("cache and release", "Cache-22", "cache money"));
        
        assertEquals(expected, actual);
    }
}
package org.cbioportal.persistence.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EhCacheUtilsTest {

    @InjectMocks
    private EhCacheUtils ehCacheUtils;

    @Mock
    private Cache cache;

    @Mock
    private CacheManager cacheManager;
    
    private String cacheName = "test_cache";
    
    @Before
    public void setUp() throws Exception {
        
        List<javax.cache.Cache.Entry<String, Object>> keysInCache = new ArrayList<>();
        keysInCache.add(createEntry("a_study1_a"));
        keysInCache.add(createEntry("a__a"));
        
        when(cache.iterator()).thenReturn(keysInCache.iterator());
        when(cacheManager.getCache(eq(cacheName))).thenReturn(cache);
    }
    
    private Cache.Entry createEntry(String key) {
        Cache.Entry mock = mock(Cache.Entry.class);
        when(mock.getKey()).thenReturn(key);
        return mock;
    }

    @Test
    public void evictByPatternAll() {
        ehCacheUtils.evictByPattern(cacheName, ".*");
        verify(cache, times(1)).remove(eq("a_study1_a"));
        verify(cache, times(1)).remove(eq("a__a"));
    }

    @Test
    public void evictByPatternOnlyStudy() {
        ehCacheUtils.evictByPattern(cacheName, "^(?=.*study1).*");
        verify(cache, times(1)).remove(eq("a_study1_a"));
        verify(cache, never()).remove(eq("a__a"));
    }

    @Test
    public void evictByPatternNotStudy() {
        ehCacheUtils.evictByPattern(cacheName, "^(?!.*study1).*");
        verify(cache, never()).remove(eq("a_study1_a"));
        verify(cache, times(1)).remove(eq("a__a"));
    }
}
package org.cbioportal.service.impl;

import org.cbioportal.persistence.cachemaputil.StaticRefCacheMapUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceImplTest {

    @InjectMocks
    private CacheServiceImpl cachingService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private StaticRefCacheMapUtil cacheMapUtil;
    private Cache mockCache;

    @Before
    public void init() {
        mockCache = mock(Cache.class);
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("name_1", "name_2"));
        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
    }

    @Test
    public void evictAllCachesSuccess() {
        cachingService.clearCaches(true);
        verify(mockCache, times(2)).clear();
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }

    @Test
    public void evictAllCachesNullManager() {
        ReflectionTestUtils.setField(cachingService, "cacheManager", null);
        cachingService.clearCaches(true);
        verify(mockCache, never()).clear();
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
        ReflectionTestUtils.setField(cachingService, "cacheManager", cacheManager);
    }
    
    @Test
    public void evictAllCachesSkipSpringManagedCache() {
        cachingService.clearCaches(false);
        verify(mockCache, never()).clear();
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }
    
}
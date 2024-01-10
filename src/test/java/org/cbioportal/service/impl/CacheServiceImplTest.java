package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.cachemaputil.StaticRefCacheMapUtil;
import org.cbioportal.persistence.util.CacheUtils;
import org.cbioportal.service.exception.CacheOperationException;
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
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceImplTest {

    @InjectMocks
    private CacheServiceImpl cachingService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private StaticRefCacheMapUtil cacheMapUtil;
    
    @Mock
    private CacheUtils cacheUtils;
    
    private Cache mockCache;
    private String clearAllKeysRegex = ".*";

    @Mock
    private StudyRepository studyRepository;
    
    @Before
    public void init() {
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("name_1", "name_2"));
        CancerStudy cancerStudy1 = mock(CancerStudy.class);
        when(cancerStudy1.getCancerStudyIdentifier()).thenReturn("study1");
        CancerStudy cancerStudy2 = mock(CancerStudy.class);
        when(cancerStudy2.getCancerStudyIdentifier()).thenReturn("study2");
        List<CancerStudy> studies = Arrays.asList(cancerStudy1, cancerStudy2);
        when(studyRepository.getAllStudies(nullable(String.class), nullable(String.class), nullable(Integer.class), nullable(Integer.class), nullable(String.class), nullable(String.class))).thenReturn(studies);
    }

    @Test
    public void evictAllCachesSuccess() throws Exception {
        cachingService.clearCaches(true);
        verify(cacheUtils, times(2)).evictByPattern(anyString(), eq(clearAllKeysRegex));
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }

    @Test
    public void evictAllCachesNullManager() throws Exception {
        ReflectionTestUtils.setField(cachingService, "cacheManager", null);
        cachingService.clearCaches(true);
        verify(cacheUtils, never()).evictByPattern(anyString(),anyString());
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
        ReflectionTestUtils.setField(cachingService, "cacheManager", cacheManager);
    }
    
    @Test
    public void evictAllCachesSkipSpringManagedCache() throws Exception {
        cachingService.clearCaches(false);
        verify(cacheUtils, never()).evictByPattern(anyString(), anyString());
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }

    @Test(expected = CacheOperationException.class)
    public void evictAllCachesThrowsException() throws Exception {
        doThrow(RuntimeException.class).when(cacheUtils).evictByPattern(anyString(), anyString());
        cachingService.clearCaches(true);
    }

    @Test
    public void evictCacheForStudySuccess() throws Exception {
        List<String> studiesInPortal = Arrays.asList("study1", "study2");
        cachingService.clearCachesForStudy("study3", true);
        verify(cacheUtils, times(2)).evictByPattern(anyString(), eq("^(?=.*study3).*|^(?!.*study3)(?!.*study1)(?!.*study2).*"));
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }

    @Test
    public void evictCacheForStudyNullManager() throws Exception {
        ReflectionTestUtils.setField(cachingService, "cacheManager", null);
        List<String> studiesInPortal = Arrays.asList("study1", "study2");
        cachingService.clearCachesForStudy("study3", true);
        verify(cacheUtils, never()).evictByPattern(anyString(), anyString());
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
        ReflectionTestUtils.setField(cachingService, "cacheManager", cacheManager);
    }

    @Test
    public void evictCacheForStudySkipSpringManagedCache() throws Exception {
        List<String> studiesInPortal = Arrays.asList("study1", "study2");
        cachingService.clearCachesForStudy("study3", false);
        verify(cacheUtils, never()).evictByPattern(anyString(), anyString());
        verify(cacheMapUtil, times(1)).initializeCacheMemory();
    }

    @Test(expected = CacheOperationException.class)
    public void evictCacheForStudyThrowsException() throws Exception {
        List<String> studiesInPortal = Arrays.asList("study1", "study2");
        doThrow(RuntimeException.class).when(cacheUtils).evictByPattern(anyString(), anyString());
        cachingService.clearCachesForStudy("study3", true);
    }
    
}
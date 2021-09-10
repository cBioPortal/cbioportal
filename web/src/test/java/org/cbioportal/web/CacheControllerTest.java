package org.cbioportal.web;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.CacheService;
import org.cbioportal.service.exception.CacheOperationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class CacheControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CacheController cacheController;
    
    // ---- Imitate @MockBean annotations of Spring Boot
    @Autowired
    private CacheService cacheService;
    @Bean
    public CacheService cacheService() {
        return mock(CacheService.class);
    }
    // ----

    @Before
    public void setUp() throws Exception {
        reset(cacheService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void clearAllCachesNoKeyProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        verify(cacheService, never()).clearCaches(true);
    }

    @Test
    public void clearAllCachesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "incorrect-key"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCaches(true);
    }

    @Test
    public void clearAllCachesSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCaches(true);
    }

    @Test
    public void clearAllCachesDisabled() throws Exception {
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCaches(true);
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", true);
    }

    @Test
    public void clearAllCachesSkipSpringManaged() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache").param("springManagedCache", "false")
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCaches(false);
    }

    @Ignore // Unable to to configure context with the GlobalExceptionHandler ControllerAdvise.
    @Test
    public void clearAllCachesServiceException() throws Exception {
        doThrow(CacheOperationException.class).when(cacheService).clearCaches(anyBoolean());
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
                .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCaches(true);
    }
    
    @Test
    public void clearCacheForStudyNoKeyProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        verify(cacheService, never()).clearCachesForStudy(anyString(), anyBoolean());
    }

    @Test
    public void clearCacheForStudyUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0")
                .header("X-API-KEY", "incorrect-key"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCachesForStudy(anyString(), anyBoolean());
    }

    @Test
    public void clearCacheForStudySuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0")
                .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCachesForStudy(eq("study_es_0"), anyBoolean());
    }

    @Test
    public void clearCacheForStudyDisabled() throws Exception {
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0")
                .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCachesForStudy(anyString(), anyBoolean());
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", true);
    }

    @Test
    public void clearCacheForStudySkipSpringManaged() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0").param("springManagedCache", "false")
                .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCachesForStudy(anyString(), eq(false));
    }

    @Ignore // Unable to to configure context with the GlobalExceptionHandler ControllerAdvise.
    @Test
    public void clearCacheForStudyServiceException() throws Exception {
        doThrow(CacheOperationException.class).when(cacheService).clearCachesForStudy(eq("study_es_0"), anyBoolean());
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache/study_es_0")
                .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCachesForStudy(anyString(), eq(true));
    }

}

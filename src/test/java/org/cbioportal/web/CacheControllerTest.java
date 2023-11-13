package org.cbioportal.web;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import org.cbioportal.service.CacheService;
import org.cbioportal.service.exception.CacheOperationException;
import org.junit.Ignore;
import org.cbioportal.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(CacheController.class)
@ContextConfiguration(classes = {CacheController.class, TestConfig.class})
@TestPropertySource(
    properties = {
        "cache.endpoint.enabled=true",
        "cache.endpoint.api-key=correct-key"
    }
)
public class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheService cacheService;

    @Autowired
    private CacheController cacheController;
    
    @Test
    @WithMockUser
    public void clearAllCachesNoKeyProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cache").with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        verify(cacheService, never()).clearCaches(true);
    }

    @Test
    @WithMockUser
    public void clearAllCachesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cache").with(csrf())
            .header("X-API-KEY", "incorrect-key"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCaches(true);
    }

    @Test
    @WithMockUser
    public void clearAllCachesSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cache").with(csrf())
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCaches(true);
    }

    @Test
    @WithMockUser
    public void clearAllCachesDisabled() throws Exception {
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cache").with(csrf())
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).clearCaches(true);
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", true);
    }

    @Test
    @WithMockUser
    public void clearAllCachesSkipSpringManaged() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cache").param("springManagedCache", "false").with(csrf())
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).clearCaches(false);
    }
}

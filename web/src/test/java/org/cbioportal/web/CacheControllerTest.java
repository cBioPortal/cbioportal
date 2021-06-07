package org.cbioportal.web;

import org.cbioportal.service.CacheService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@TestPropertySource(
    properties = {
        // -- needed for the PropertySourcesPlaceholderConfigurer
        "PORTAL_HOME=fake",
        "google.analytics.tracking.code.api=",
        "google.analytics.application.client.id=1"
        // --
    }
)
@Configuration
public class CacheControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheController cacheController;
    
    @Bean
    public CacheService cacheService() {
        return mock(CacheService.class);
    }

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void clearAllCachesNoKeyProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        verify(cacheService, never()).evictAllCaches();
    }

    @Test
    public void clearAllCachesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "incorrect-key"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).evictAllCaches();
    }

    @Test
    public void clearAllCachesSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, times(1)).evictAllCaches();
    }

    @Test
    public void clearAllCachesDisabled() throws Exception {
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/cache")
            .header("X-API-KEY", "correct-key"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
        verify(cacheService, never()).evictAllCaches();
        ReflectionTestUtils.setField(cacheController, "cacheEndpointEnabled", true);
    }

}

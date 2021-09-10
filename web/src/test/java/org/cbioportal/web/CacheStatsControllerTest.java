package org.cbioportal.web;

import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {CacheStatsController.class, TestConfig.class})
@TestPropertySource(
    properties = "persistence.cache_type=redis"
)
public class CacheStatsControllerTest {

    public static final String VALID_CACHE_ALIAS = "GeneralRepositoryCache";
    public static final String INVALID_CACHE_ALIAS = "InvalidCacheForTesting";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public CacheStatisticsService cacheStatisticsService;

    @Before
    public void setUp() throws Exception {
        Mockito.when(cacheStatisticsService.getKeyCountsPerClass(Mockito.anyString())).thenAnswer(new Answer<List<String>>() {
            public List<String> answer(InvocationOnMock invocation) throws CacheNotFoundException {
                Object[] args = invocation.getArguments();
                String cacheAlias = (String)args[0];
                if (VALID_CACHE_ALIAS.equals(cacheAlias)) {
                    return new ArrayList<String>();
                }
                throw new CacheNotFoundException(cacheAlias);
            }
        });
        Mockito.when(cacheStatisticsService.getKeysInCache(Mockito.anyString())).thenAnswer(new Answer<List<String>>() {
            public List<String> answer(InvocationOnMock invocation) throws CacheNotFoundException {
                Object[] args = invocation.getArguments();
                String cacheAlias = (String)args[0];
                if (VALID_CACHE_ALIAS.equals(cacheAlias)) {
                    return new ArrayList<String>();
                }
                throw new CacheNotFoundException(cacheAlias);
            }
        });
    }

    @Test
    @WithMockUser
    public void testGetKeysInCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/" + VALID_CACHE_ALIAS + "/keysInCache")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    public void testGetKeysInInvalidCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/" + INVALID_CACHE_ALIAS + "/keysInCache")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGetKeyCountsPerClass() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/" + VALID_CACHE_ALIAS + "/keyCountsPerClass")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    public void testGetKeyCountsPerClassInvalidCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/" + INVALID_CACHE_ALIAS + "/keyCountsPerClass")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}

package org.cbioportal.web;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@ActiveProfiles(profiles = "redis")
@Configuration
public class CacheStatsControllerTest {

    public static final String VALID_CACHE_ALIAS = "GeneralRepositoryCache";

    public static final String INVALID_CACHE_ALIAS = "InvalidCacheForTesting";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    @Qualifier("mockCacheStatisticsService")
    public CacheStatisticsService cacheStatisticsService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Bean
    @Qualifier("mockCacheStatisticsService")
    public static CacheStatisticsService cacheStatisticsService() throws CacheNotFoundException {
        CacheStatisticsService cacheStatisticsServiceMock = Mockito.mock(CacheStatisticsService.class);
        Mockito.when(cacheStatisticsServiceMock.getKeyCountsPerClass(Mockito.anyString())).thenAnswer(new Answer<List<String>>() {
            public List<String> answer(InvocationOnMock invocation) throws CacheNotFoundException {
                Object[] args = invocation.getArguments();
                String cacheAlias = (String)args[0];
                if (VALID_CACHE_ALIAS.equals(cacheAlias)) {
                    return new ArrayList<String>();
                }
                throw new CacheNotFoundException(cacheAlias);
            }
        });
        Mockito.when(cacheStatisticsServiceMock.getKeysInCache(Mockito.anyString())).thenAnswer(new Answer<List<String>>() {
            public List<String> answer(InvocationOnMock invocation) throws CacheNotFoundException {
                Object[] args = invocation.getArguments();
                String cacheAlias = (String)args[0];
                if (VALID_CACHE_ALIAS.equals(cacheAlias)) {
                    return new ArrayList<String>();
                }
                throw new CacheNotFoundException(cacheAlias);
            }
        });
        return cacheStatisticsServiceMock;
    }

    @Test
    public void testGetKeysInCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/" + VALID_CACHE_ALIAS + "/keysInCache")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetKeysInInvalidCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/" + INVALID_CACHE_ALIAS + "/keysInCache")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetKeyCountsPerClass() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/" + VALID_CACHE_ALIAS + "/keyCountsPerClass")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetKeyCountsPerClassInvalidCache() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/" + INVALID_CACHE_ALIAS + "/keyCountsPerClass")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}

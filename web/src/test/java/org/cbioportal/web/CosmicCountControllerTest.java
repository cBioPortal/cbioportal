package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CosmicMutation;
import org.cbioportal.service.CosmicCountService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class CosmicCountControllerTest {

    private static final String TEST_COSMIC_MUTATION_ID_1 = "test_cosmic_mutation_id_1";
    private static final String TEST_PROTEIN_CHANGE_1 = "test_protein_change_1";
    private static final String TEST_KEYWORD_1 = "test_keyword_1";
    private static final int TEST_COUNT_1 = 1;
    private static final String TEST_COSMIC_MUTATION_ID_2 = "test_cosmic_mutation_id_2";
    private static final String TEST_PROTEIN_CHANGE_2 = "test_protein_change_2";
    private static final String TEST_KEYWORD_2 = "test_keyword_2";
    private static final int TEST_COUNT_2 = 2;
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CosmicCountService cosmicCountService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public CosmicCountService cosmicCountService() {
        return Mockito.mock(CosmicCountService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(cosmicCountService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchCosmicCounts() throws Exception {

        List<CosmicMutation> cosmicMutationList = new ArrayList<>();
        CosmicMutation cosmicMutation1 = new CosmicMutation();
        cosmicMutation1.setCosmicMutationId(TEST_COSMIC_MUTATION_ID_1);
        cosmicMutation1.setProteinChange(TEST_PROTEIN_CHANGE_1);
        cosmicMutation1.setKeyword(TEST_KEYWORD_1);
        cosmicMutation1.setCount(TEST_COUNT_1);
        cosmicMutationList.add(cosmicMutation1);
        CosmicMutation cosmicMutation2 = new CosmicMutation();
        cosmicMutation2.setCosmicMutationId(TEST_COSMIC_MUTATION_ID_2);
        cosmicMutation2.setProteinChange(TEST_PROTEIN_CHANGE_2);
        cosmicMutation2.setKeyword(TEST_KEYWORD_2);
        cosmicMutation2.setCount(TEST_COUNT_2);
        cosmicMutationList.add(cosmicMutation2);

        Mockito.when(cosmicCountService.fetchCosmicCountsByKeywords(Mockito.anyList()))
            .thenReturn(cosmicMutationList);

        List<String> keywords = new ArrayList<>();
        keywords.add(TEST_KEYWORD_1);
        keywords.add(TEST_KEYWORD_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/cosmic-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(keywords)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cosmicMutationId").value(TEST_COSMIC_MUTATION_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value(TEST_PROTEIN_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cosmicMutationId").value(TEST_COSMIC_MUTATION_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinChange").value(TEST_PROTEIN_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_COUNT_2));
    }
}

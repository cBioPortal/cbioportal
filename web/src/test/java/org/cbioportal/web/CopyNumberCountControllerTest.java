package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.service.CopyNumberCountService;
import org.cbioportal.web.parameter.CopyNumberCountIdentifier;
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
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class CopyNumberCountControllerTest {

    private static final String TEST_GENETIC_PROFILE_STABLE_ID = "test_genetic_profile_stable_id";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final int TEST_ALTERATION_1 = -2;
    private static final int TEST_NUMBER_OF_SAMPLES_1 = 6;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1 = 4;
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final int TEST_ALTERATION_2 = 2;
    private static final int TEST_NUMBER_OF_SAMPLES_2 = 10;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2 = 8;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CopyNumberCountService copyNumberCountService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public CopyNumberCountService copyNumberCountService() {
        return Mockito.mock(CopyNumberCountService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(copyNumberCountService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void fetchCopyNumberCounts() throws Exception {

        List<CopyNumberCount> copyNumberCountList = new ArrayList<>();
        CopyNumberCount copyNumberCount1 = new CopyNumberCount();
        copyNumberCount1.setGeneticProfileId(TEST_GENETIC_PROFILE_STABLE_ID);
        copyNumberCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        copyNumberCount1.setAlteration(TEST_ALTERATION_1);
        copyNumberCount1.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_1);
        copyNumberCount1.setNumberOfSamplesWithAlterationInGene(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1);
        copyNumberCountList.add(copyNumberCount1);
        CopyNumberCount copyNumberCount2 = new CopyNumberCount();
        copyNumberCount2.setGeneticProfileId(TEST_GENETIC_PROFILE_STABLE_ID);
        copyNumberCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        copyNumberCount2.setAlteration(TEST_ALTERATION_2);
        copyNumberCount2.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_2);
        copyNumberCount2.setNumberOfSamplesWithAlterationInGene(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2);
        copyNumberCountList.add(copyNumberCount2);

        Mockito.when(copyNumberCountService.fetchCopyNumberCounts(Mockito.anyString(), Mockito.anyListOf(Integer.class),
            Mockito.anyListOf(Integer.class))).thenReturn(copyNumberCountList);

        List<CopyNumberCountIdentifier> copyNumberCountIdentifiers = new ArrayList<>();
        CopyNumberCountIdentifier copyNumberCountIdentifier1 = new CopyNumberCountIdentifier();
        copyNumberCountIdentifier1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        copyNumberCountIdentifier1.setAlteration(TEST_ALTERATION_1);
        copyNumberCountIdentifiers.add(copyNumberCountIdentifier1);
        CopyNumberCountIdentifier copyNumberCountIdentifier2 = new CopyNumberCountIdentifier();
        copyNumberCountIdentifier2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        copyNumberCountIdentifier2.setAlteration(TEST_ALTERATION_2);
        copyNumberCountIdentifiers.add(copyNumberCountIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/genetic-profiles/test_genetic_profile_id/copy-number-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(copyNumberCountIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamplesWithAlterationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamplesWithAlterationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2));
    }
}
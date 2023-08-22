package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.VariantCount;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.web.parameter.VariantCountIdentifier;
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
public class VariantCountControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID = "test_molecular_profile_stable_id";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_KEYWORD_1 = "test_keyword_1";
    private static final int TEST_NUMBER_OF_SAMPLES_1 = 6;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_1 = 4;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_1 = 1;
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_KEYWORD_2 = "test_keyword_2";
    private static final int TEST_NUMBER_OF_SAMPLES_2 = 10;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_2 = 8;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_2 = 2;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private VariantCountService variantCountService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public VariantCountService variantCountService() {
        return Mockito.mock(VariantCountService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(variantCountService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getVariantCounts() throws Exception {

        List<VariantCount> variantCountList = new ArrayList<>();
        VariantCount variantCount1 = new VariantCount();
        variantCount1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID);
        variantCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        variantCount1.setKeyword(TEST_KEYWORD_1);
        variantCount1.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_1);
        variantCount1.setNumberOfSamplesWithMutationInGene(TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_1);
        variantCount1.setNumberOfSamplesWithKeyword(TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_1);
        variantCountList.add(variantCount1);
        VariantCount variantCount2 = new VariantCount();
        variantCount2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID);
        variantCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        variantCount2.setKeyword(TEST_KEYWORD_2);
        variantCount2.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_2);
        variantCount2.setNumberOfSamplesWithMutationInGene(TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_2);
        variantCount2.setNumberOfSamplesWithKeyword(TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_2);
        variantCountList.add(variantCount2);

        Mockito.when(variantCountService.fetchVariantCounts(Mockito.anyString(), Mockito.anyList(),
            Mockito.anyList())).thenReturn(variantCountList);

        List<VariantCountIdentifier> variantCountIdentifiers = new ArrayList<>();
        VariantCountIdentifier variantCountIdentifier1 = new VariantCountIdentifier();
        variantCountIdentifier1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        variantCountIdentifier1.setKeyword(TEST_KEYWORD_1);
        variantCountIdentifiers.add(variantCountIdentifier1);
        VariantCountIdentifier variantCountIdentifier2 = new VariantCountIdentifier();
        variantCountIdentifier2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        variantCountIdentifier2.setKeyword(TEST_KEYWORD_2);
        variantCountIdentifiers.add(variantCountIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/molecular-profiles/test_molecular_profile_id/variant-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(variantCountIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamplesWithMutationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamplesWithKeyword")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamplesWithMutationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_MUTATION_IN_GENE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamplesWithKeyword")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_KEYWORD_2));
    }
}

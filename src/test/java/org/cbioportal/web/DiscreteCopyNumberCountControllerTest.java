package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.CopyNumberCountIdentifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {DiscreteCopyNumberCountController.class, TestConfig.class})
public class DiscreteCopyNumberCountControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_1 = "test_molecular_profile_stable_id_1";
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final int TEST_ALTERATION_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_TYPE_1 = "test_type_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final String TEST_CHROMOSOME_1 = "test_chromosome_1";
    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_2 = "test_molecular_profile_stable_id_2";
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final int TEST_ALTERATION_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_TYPE_2 = "test_type_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final String TEST_CHROMOSOME_2 = "test_chromosome_2";
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";
    private static final int TEST_NUMBER_OF_SAMPLES_1 = 6;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1 = 4;
    private static final int TEST_NUMBER_OF_SAMPLES_2 = 10;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2 = 8;

    @MockBean
    private DiscreteCopyNumberService discreteCopyNumberService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchCopyNumberCounts() throws Exception {

        List<CopyNumberCount> copyNumberCountList = new ArrayList<>();
        CopyNumberCount copyNumberCount1 = new CopyNumberCount();
        copyNumberCount1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        copyNumberCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        copyNumberCount1.setAlteration(TEST_ALTERATION_1);
        copyNumberCount1.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_1);
        copyNumberCount1.setNumberOfSamplesWithAlterationInGene(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1);
        copyNumberCountList.add(copyNumberCount1);
        CopyNumberCount copyNumberCount2 = new CopyNumberCount();
        copyNumberCount2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        copyNumberCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        copyNumberCount2.setAlteration(TEST_ALTERATION_2);
        copyNumberCount2.setNumberOfSamples(TEST_NUMBER_OF_SAMPLES_2);
        copyNumberCount2.setNumberOfSamplesWithAlterationInGene(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2);
        copyNumberCountList.add(copyNumberCount2);

        Mockito.when(discreteCopyNumberService.fetchCopyNumberCounts(Mockito.anyString(),
            Mockito.anyList(), Mockito.anyList())).thenReturn(copyNumberCountList);

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
            .post("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number-counts/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(copyNumberCountIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfSamplesWithAlterationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamples").value(TEST_NUMBER_OF_SAMPLES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfSamplesWithAlterationInGene")
                .value(TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2));
    }
}

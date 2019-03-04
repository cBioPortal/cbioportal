package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.CopyNumberEnrichmentService;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.AlterationCount;
import org.cbioportal.model.MolecularProfileCase;
import org.cbioportal.web.parameter.MultipleStudiesEnrichmentFilter;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class CopyNumberEnrichmentControllerTest {

    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final int TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1 = 1;
    private static final int TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1 = 1;
    private static final String TEST_LOG_RATIO_1 = "1";
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(1.1);
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final int TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2 = 2;
    private static final int TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2 = 2;
    private static final String TEST_LOG_RATIO_2 = "2";
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(2.1);
    private static final int TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1 = 1;
    private static final int TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2 = 1;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CopyNumberEnrichmentService copyNumberEnrichmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Bean
    public CopyNumberEnrichmentService copyNumberEnrichmentService() {
        return Mockito.mock(CopyNumberEnrichmentService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(copyNumberEnrichmentService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchCopyNumberEnrichments() throws Exception {

        List<AlterationEnrichment> alterationEnrichments = new ArrayList<>();
        AlterationEnrichment alterationEnrichment1 = new AlterationEnrichment();
        AlterationCount alterationEnrichment1Set1Count = new AlterationCount();
        AlterationCount alterationEnrichment1Set2Count = new AlterationCount();
        alterationEnrichment1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        alterationEnrichment1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        alterationEnrichment1.setCytoband(TEST_CYTOBAND_1);
        alterationEnrichment1.setLogRatio(TEST_LOG_RATIO_1);
        alterationEnrichment1.setpValue(TEST_P_VALUE_1);
        alterationEnrichment1Set1Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1);
        alterationEnrichment1Set1Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1);
        alterationEnrichment1Set2Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1);
        alterationEnrichment1Set2Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2);
        alterationEnrichment1.setSet1AlterationCount(alterationEnrichment1Set1Count);
        alterationEnrichment1.setSet2AlterationCount(alterationEnrichment1Set2Count);
        alterationEnrichments.add(alterationEnrichment1);

        AlterationEnrichment alterationEnrichment2 = new AlterationEnrichment();
        AlterationCount alterationEnrichment2Set1Count = new AlterationCount();
        AlterationCount alterationEnrichment2Set2Count = new AlterationCount();
        alterationEnrichment2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        alterationEnrichment2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        alterationEnrichment2.setCytoband(TEST_CYTOBAND_2);
        alterationEnrichment2.setLogRatio(TEST_LOG_RATIO_2);
        alterationEnrichment2.setpValue(TEST_P_VALUE_2);
        alterationEnrichment2Set1Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2);
        alterationEnrichment2Set1Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1);
        alterationEnrichment2Set2Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2);
        alterationEnrichment2Set2Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2);
        alterationEnrichment2.setSet1AlterationCount(alterationEnrichment2Set1Count);
        alterationEnrichment2.setSet2AlterationCount(alterationEnrichment2Set2Count);

        alterationEnrichments.add(alterationEnrichment2);

        Mockito.when(copyNumberEnrichmentService.getCopyNumberEnrichments(Mockito.anyListOf(MolecularProfileCase.class),
                Mockito.anyListOf(MolecularProfileCase.class), Mockito.anyListOf(Integer.class),
            Mockito.anyString())).thenReturn(alterationEnrichments);

        MolecularProfileCase entity1 = new MolecularProfileCase();
        entity1.setCaseId("test_sample_id_1");
        entity1.setMolecularProfileId("test_1_mutations");
        MolecularProfileCase entity2 = new MolecularProfileCase();
        entity2.setCaseId("test_sample_id_2");
        entity2.setMolecularProfileId("test_2_mutations");
        MultipleStudiesEnrichmentFilter multiStudyEnrichmentFilter = new MultipleStudiesEnrichmentFilter();
        multiStudyEnrichmentFilter.setMolecularProfileCaseSet1(Arrays.asList(entity1));
        multiStudyEnrichmentFilter.setMolecularProfileCaseSet2(Arrays.asList(entity2));

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/copy-number-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(multiStudyEnrichmentFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].set1AlterationCount.alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].set2AlterationCount.alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].logRatio").value(TEST_LOG_RATIO_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].set1AlterationCount.profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].set2AlterationCount.profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].set1AlterationCount.alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].set2AlterationCount.alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].logRatio").value(TEST_LOG_RATIO_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].set1AlterationCount.profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].set2AlterationCount.profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2));
    }
}

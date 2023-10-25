package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.service.AlterationEnrichmentService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.web.util.AlterationFilterMockitoArgumentMatcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(AlterationEnrichmentControllerTest.class)
@ContextConfiguration(classes={AlterationEnrichmentController.class, TestConfig.class})
public class AlterationEnrichmentControllerTest {

    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final int TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1 = 1;
    private static final int TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1 = 1;
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(1.1);
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final int TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2 = 2;
    private static final int TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2 = 2;
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(2.1);
    private static final int TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1 = 1;
    private static final int TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2 = 1;

    @MockBean
    private AlterationEnrichmentService alterationEnrichmentService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ArrayList<AlterationEnrichment> alterationEnrichments;
    Map<MutationEventType, Boolean> mutationTypes;
    Map<CNA, Boolean> cnaTypes;

    private MolecularProfileCasesGroupAndAlterationTypeFilter filter;

    private static class caseIdMatcher implements ArgumentMatcher<Map> {
        @Override
        public boolean matches(Map map) {
            Map<String, List<MolecularProfileCaseIdentifier>> e = (Map<String, List<MolecularProfileCaseIdentifier>>) map;
            return e.containsKey("altered group")
                && e.containsKey("unaltered group")
                && e.get("altered group").size() == 1
                && e.get("unaltered group").size() == 1
                && e.get("altered group").get(0).getCaseId().equals("test_sample_id_1")
                && e.get("unaltered group").get(0).getCaseId().equals("test_sample_id_2");
        }
    }
        
    @Before
    public void setUp() throws Exception {
        Mockito.reset(alterationEnrichmentService);

        alterationEnrichments = new ArrayList<>();
        AlterationEnrichment alterationEnrichment1 = new AlterationEnrichment();
        CountSummary alterationEnrichment1Set1Count = new CountSummary();
        CountSummary alterationEnrichment1Set2Count = new CountSummary();
        alterationEnrichment1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        alterationEnrichment1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        alterationEnrichment1.setCytoband(TEST_CYTOBAND_1);
        alterationEnrichment1.setpValue(TEST_P_VALUE_1);
        alterationEnrichment1Set1Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1);
        alterationEnrichment1Set1Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1);
        alterationEnrichment1Set2Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1);
        alterationEnrichment1Set2Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2);
        alterationEnrichment1.setCounts(Arrays.asList(alterationEnrichment1Set1Count,alterationEnrichment1Set2Count));
        alterationEnrichments.add(alterationEnrichment1);

        AlterationEnrichment alterationEnrichment2 = new AlterationEnrichment();
        CountSummary alterationEnrichment2Set1Count = new CountSummary();
        CountSummary alterationEnrichment2Set2Count = new CountSummary();
        alterationEnrichment2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        alterationEnrichment2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        alterationEnrichment2.setCytoband(TEST_CYTOBAND_2);
        alterationEnrichment2.setpValue(TEST_P_VALUE_2);
        alterationEnrichment2Set1Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2);
        alterationEnrichment2Set1Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1);
        alterationEnrichment2Set2Count.setAlteredCount(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2);
        alterationEnrichment2Set2Count.setProfiledCount(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2);
        alterationEnrichment2.setCounts(Arrays.asList(alterationEnrichment2Set1Count,alterationEnrichment2Set2Count));
        alterationEnrichments.add(alterationEnrichment2);

        MolecularProfileCaseIdentifier entity1 = new MolecularProfileCaseIdentifier();
        entity1.setCaseId("test_sample_id_1");
        entity1.setMolecularProfileId("test_1_mutations");
        MolecularProfileCaseIdentifier entity2 = new MolecularProfileCaseIdentifier();
        entity2.setCaseId("test_sample_id_2");
        entity2.setMolecularProfileId("test_2_mutations");

        MolecularProfileCasesGroupFilter casesGroup1 = new MolecularProfileCasesGroupFilter();
        casesGroup1.setName("altered group");
        casesGroup1.setMolecularProfileCaseIdentifiers(Arrays.asList(entity1));

        MolecularProfileCasesGroupFilter casesGroup2 = new MolecularProfileCasesGroupFilter();
        casesGroup2.setName("unaltered group");
        casesGroup2.setMolecularProfileCaseIdentifiers(Arrays.asList(entity2));

        filter = new MolecularProfileCasesGroupAndAlterationTypeFilter();
        filter.setMolecularProfileCasesGroupFilter(Arrays.asList(casesGroup1,casesGroup2));

        filter.setAlterationEventTypes(new AlterationFilter());
        
        mutationTypes = new HashMap<>();
        cnaTypes = new HashMap<>();
    }

    @Test
    @WithMockUser
    public void fetchAlterationEnrichmentsAllTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            any(),
            argThat(new AlterationFilterMockitoArgumentMatcher("ALL", "ALL")))).thenReturn(alterationEnrichments);

        mutationTypes.put(MutationEventType.missense_mutation, true);
        mutationTypes.put(MutationEventType.feature_truncation, true);
        cnaTypes.put(CNA.AMP, true);
        cnaTypes.put(CNA.HOMDEL, true);

        filter.getAlterationEventTypes().setMutationEventTypes(mutationTypes);
        filter.getAlterationEventTypes().setCopyNumberAlterationEventTypes(cnaTypes);
        
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/alteration-enrichments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2));
    }

    @Test
    @WithMockUser
    public void fetchAlterationEnrichmentsNoTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            any(),
            argThat(new AlterationFilterMockitoArgumentMatcher("EMPTY", "EMPTY")))).thenReturn(alterationEnrichments);
        
        mutationTypes.put(MutationEventType.missense_mutation, false);
        mutationTypes.put(MutationEventType.feature_truncation, false);
        cnaTypes.put(CNA.AMP, false);
        cnaTypes.put(CNA.HOMDEL, false);
        
        filter.getAlterationEventTypes().setMutationEventTypes(mutationTypes);
        filter.getAlterationEventTypes().setCopyNumberAlterationEventTypes(cnaTypes);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/alteration-enrichments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2));
    }

    @Test
    @WithMockUser
    public void fetchAlterationEnrichmentsAllMutationTypesDeselected() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            any(),
            argThat(new AlterationFilterMockitoArgumentMatcher("EMPTY", "ALL"))
        )).thenReturn(alterationEnrichments);

        mutationTypes.put(MutationEventType.missense_mutation, false);
        mutationTypes.put(MutationEventType.feature_truncation, false);
        cnaTypes.put(CNA.AMP, true);
        cnaTypes.put(CNA.HOMDEL, true);

        filter.getAlterationEventTypes().setMutationEventTypes(mutationTypes);
        filter.getAlterationEventTypes().setCopyNumberAlterationEventTypes(cnaTypes);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/alteration-enrichments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2));
    }

    @Test
    @WithMockUser
    public void fetchAlterationEnrichmentsAllCnaTypesDeselected() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            any(),
            argThat(new AlterationFilterMockitoArgumentMatcher("ALL", "EMPTY"))
        )).thenReturn(alterationEnrichments);

        mutationTypes.put(MutationEventType.missense_mutation, true);
        mutationTypes.put(MutationEventType.feature_truncation, true);
        cnaTypes.put(CNA.AMP, false);
        cnaTypes.put(CNA.HOMDEL, false);

        filter.getAlterationEventTypes().setMutationEventTypes(mutationTypes);
        filter.getAlterationEventTypes().setCopyNumberAlterationEventTypes(cnaTypes);
        
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/alteration-enrichments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    @WithMockUser
    public void fetchAlterationEnrichmentsSubsetTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            any(),
            argThat(new AlterationFilterMockitoArgumentMatcher("SOME", "SOME")))).thenReturn(alterationEnrichments);

        mutationTypes.put(MutationEventType.missense_mutation, false);
        mutationTypes.put(MutationEventType.feature_truncation, true);
        cnaTypes.put(CNA.AMP, false);
        cnaTypes.put(CNA.HOMDEL, true);

        filter.getAlterationEventTypes().setMutationEventTypes(mutationTypes);
        filter.getAlterationEventTypes().setCopyNumberAlterationEventTypes(cnaTypes);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/alteration-enrichments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].alteredCount").value(TEST_NUMBER_OF_SAMPLES_ALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].alteredCount").value(TEST_NUMBER_OF_SAMPLES_UNALTERED_IN_SET_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].profiledCount").value(TEST_NUMBER_OF_SAMPLES_PROFILED_IN_SET_2));
    }
}

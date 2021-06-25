package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.service.AlterationEnrichmentService;
import org.cbioportal.web.parameter.AlterationEventTypeFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.web.util.SelectMockitoArgumentMatcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
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

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AlterationEnrichmentService alterationEnrichmentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private ArrayList<AlterationEnrichment> alterationEnrichments;
    private AlterationEventTypeFilter eventTypes;

    @Bean
    public AlterationEnrichmentService alterationEnrichmentService() {
        return Mockito.mock(AlterationEnrichmentService.class);
    }

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

        eventTypes = new AlterationEventTypeFilter();
        Map<MutationEventType, Boolean> mutationEventTypeMap = new HashMap();
        mutationEventTypeMap.put(MutationEventType.missense_mutation, true);
        mutationEventTypeMap.put(MutationEventType.feature_truncation, true);
        Map<CNA, Boolean> cnaEventTypeMap = new HashMap();
        cnaEventTypeMap.put(CNA.AMP, true);
        cnaEventTypeMap.put(CNA.HOMDEL, true);
        eventTypes.setMutationEventTypes(mutationEventTypeMap);
        eventTypes.setCopyNumberAlterationEventTypes(cnaEventTypeMap);
        filter.setAlterationEventTypes(eventTypes);
        
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        
    }

    @Test
    public void fetchAlterationEnrichmentsAllTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            argThat(new SelectMockitoArgumentMatcher("ALL")),
            argThat(new SelectMockitoArgumentMatcher("ALL")),
            any()))
            .thenReturn(alterationEnrichments);
        
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/alteration-enrichments/fetch")
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
    public void fetchAlterationEnrichmentsNoTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            argThat(new SelectMockitoArgumentMatcher("EMPTY")),
            argThat(new SelectMockitoArgumentMatcher("EMPTY")),
            any()))
            .thenReturn(alterationEnrichments);
        
        filter.getAlterationEventTypes().getMutationEventTypes().put(MutationEventType.missense_mutation, false);
        filter.getAlterationEventTypes().getMutationEventTypes().put(MutationEventType.feature_truncation, false);
        filter.getAlterationEventTypes().getCopyNumberAlterationEventTypes().put(CNA.AMP, false);
        filter.getAlterationEventTypes().getCopyNumberAlterationEventTypes().put(CNA.HOMDEL, false);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/alteration-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    public void fetchAlterationEnrichmentsNullMutationTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            argThat(new SelectMockitoArgumentMatcher("EMPTY")),
            argThat(new SelectMockitoArgumentMatcher("ALL")),
            any()))
            .thenReturn(alterationEnrichments);

        filter.getAlterationEventTypes().getMutationEventTypes().put(MutationEventType.missense_mutation, false);
        filter.getAlterationEventTypes().getMutationEventTypes().put(MutationEventType.feature_truncation, false);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/alteration-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    public void fetchAlterationEnrichmentsNullCnaTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            argThat(new SelectMockitoArgumentMatcher("ALL")),
            argThat(new SelectMockitoArgumentMatcher("EMPTY")),
            any()))
            .thenReturn(alterationEnrichments);

        filter.getAlterationEventTypes().getCopyNumberAlterationEventTypes().put(CNA.AMP, false);
        filter.getAlterationEventTypes().getCopyNumberAlterationEventTypes().put(CNA.HOMDEL, false);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/alteration-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    public void fetchAlterationEnrichmentsSubsetTypes() throws Exception {

        when(alterationEnrichmentService.getAlterationEnrichments(
            argThat(new caseIdMatcher()),
            argThat(new SelectMockitoArgumentMatcher("SOME")),
            argThat(new SelectMockitoArgumentMatcher("SOME")),
            any()))
            .thenReturn(alterationEnrichments);

        filter.getAlterationEventTypes().getMutationEventTypes().put(MutationEventType.missense_mutation, false);
        filter.getAlterationEventTypes().getCopyNumberAlterationEventTypes().put(CNA.HOMDEL, false);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/alteration-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }
}

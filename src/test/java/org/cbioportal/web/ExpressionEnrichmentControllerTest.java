package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.GroupStatistics;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.web.config.TestConfig;
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
@ContextConfiguration(classes = {ExpressionEnrichmentController.class, TestConfig.class})
public class ExpressionEnrichmentControllerTest {

    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final BigDecimal TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1 = new BigDecimal(2.3);
    private static final BigDecimal TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1 = new BigDecimal(2.4);
    private static final BigDecimal TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1 = new BigDecimal(2.1);
    private static final BigDecimal TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1 = new BigDecimal(2.6);
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(1.1);
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final BigDecimal TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2 = new BigDecimal(2.7);
    private static final BigDecimal TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2 = new BigDecimal(2.8);
    private static final BigDecimal TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2 = new BigDecimal(2.9);
    private static final BigDecimal TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2 = new BigDecimal(3.1);
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(2.1);

    @MockBean
    private ExpressionEnrichmentService expressionEnrichmentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchGenomicEnrichments() throws Exception {

        List<GenomicEnrichment> expressionEnrichments = new ArrayList<>();
        GenomicEnrichment expressionEnrichment1 = new GenomicEnrichment();
        expressionEnrichment1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        expressionEnrichment1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        expressionEnrichment1.setCytoband(TEST_CYTOBAND_1);
        List<GroupStatistics> groupStatistics1 = new ArrayList<>();
        GroupStatistics alteredGroupStats1 = new GroupStatistics();
        alteredGroupStats1.setName("altered samples");
        alteredGroupStats1.setMeanExpression(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1);
        alteredGroupStats1.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1);
        groupStatistics1.add(alteredGroupStats1);
        GroupStatistics unalteredGroupStats1 = new GroupStatistics();
        unalteredGroupStats1.setName("unaltered samples");
        unalteredGroupStats1.setMeanExpression(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1);
        unalteredGroupStats1.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1);
        groupStatistics1.add(unalteredGroupStats1);
        expressionEnrichment1.setpValue(TEST_P_VALUE_1);
        expressionEnrichments.add(expressionEnrichment1);
        expressionEnrichment1.setGroupsStatistics(groupStatistics1);

        GenomicEnrichment expressionEnrichment2 = new GenomicEnrichment();
        expressionEnrichment2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        expressionEnrichment2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        expressionEnrichment2.setCytoband(TEST_CYTOBAND_2);
        List<GroupStatistics> groupStatistics2 = new ArrayList<>();
        GroupStatistics alteredGroupStats2 = new GroupStatistics();
        alteredGroupStats2.setName("altered samples");
        alteredGroupStats2.setMeanExpression(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2);
        alteredGroupStats2.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2);
        groupStatistics2.add(alteredGroupStats2);
        GroupStatistics unalteredGroupStats2 = new GroupStatistics();
        unalteredGroupStats2.setName("unaltered samples");
        unalteredGroupStats2.setMeanExpression(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2);
        unalteredGroupStats2.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2);
        groupStatistics2.add(unalteredGroupStats2);
        expressionEnrichment2.setGroupsStatistics(groupStatistics2);
        expressionEnrichment2.setpValue(TEST_P_VALUE_2);
        expressionEnrichments.add(expressionEnrichment2);

        Mockito.when(expressionEnrichmentService.getGenomicEnrichments(Mockito.anyString(), Mockito.anyMap(),
                Mockito.any(EnrichmentType.class))).thenReturn(expressionEnrichments);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/expression-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(
                        "[{\"molecularProfileCaseIdentifiers\":[{\"caseId\":\"TCGA-OR-A5JH-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"},{\"caseId\":\"TCGA-OR-A5K2-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"}],\"name\":\"altered\"},"
                                + "{\"molecularProfileCaseIdentifiers\":[{\"caseId\":\"TCGA-OR-A5LN-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"},{\"caseId\":\"TCGA-OR-A5LS-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"}],\"name\":\"unaltered\"}]"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[0].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[0].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[1].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[1].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[0].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[0].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[1].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[1].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2));
    }

    @Test
    @WithMockUser
    public void fetchGenericAssayEnrichments() throws Exception {

        List<GenericAssayEnrichment> genericAssayEnrichments = new ArrayList<>();
        GenericAssayEnrichment genericAssayEnrichment1 = new GenericAssayEnrichment();
        genericAssayEnrichment1.setStableId(TEST_HUGO_GENE_SYMBOL_1);
        List<GroupStatistics> groupStatistics1 = new ArrayList<>();
        GroupStatistics alteredGroupStats1 = new GroupStatistics();
        alteredGroupStats1.setName("altered samples");
        alteredGroupStats1.setMeanExpression(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1);
        alteredGroupStats1.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1);
        groupStatistics1.add(alteredGroupStats1);
        GroupStatistics unalteredGroupStats1 = new GroupStatistics();
        unalteredGroupStats1.setName("unaltered samples");
        unalteredGroupStats1.setMeanExpression(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1);
        unalteredGroupStats1.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1);
        groupStatistics1.add(unalteredGroupStats1);
        genericAssayEnrichment1.setpValue(TEST_P_VALUE_1);
        genericAssayEnrichments.add(genericAssayEnrichment1);
        genericAssayEnrichment1.setGroupsStatistics(groupStatistics1);

        GenericAssayEnrichment genericAssayEnrichment2 = new GenericAssayEnrichment();
        genericAssayEnrichment2.setStableId(TEST_HUGO_GENE_SYMBOL_2);
        List<GroupStatistics> groupStatistics2 = new ArrayList<>();
        GroupStatistics alteredGroupStats2 = new GroupStatistics();
        alteredGroupStats2.setName("altered samples");
        alteredGroupStats2.setMeanExpression(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2);
        alteredGroupStats2.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2);
        groupStatistics2.add(alteredGroupStats2);
        GroupStatistics unalteredGroupStats2 = new GroupStatistics();
        unalteredGroupStats2.setName("unaltered samples");
        unalteredGroupStats2.setMeanExpression(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2);
        unalteredGroupStats2.setStandardDeviation(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2);
        groupStatistics2.add(unalteredGroupStats2);
        genericAssayEnrichment2.setGroupsStatistics(groupStatistics2);
        genericAssayEnrichment2.setpValue(TEST_P_VALUE_2);
        genericAssayEnrichments.add(genericAssayEnrichment2);

        Mockito.when(expressionEnrichmentService.getGenericAssayNumericalEnrichments(Mockito.anyString(), Mockito.anyMap(),
                Mockito.any(EnrichmentType.class))).thenReturn(genericAssayEnrichments);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/generic-assay-enrichments/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(
                        "[{\"molecularProfileCaseIdentifiers\":[{\"caseId\":\"TCGA-OR-A5JH-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"},{\"caseId\":\"TCGA-OR-A5K2-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"}],\"name\":\"altered\"},"
                                + "{\"molecularProfileCaseIdentifiers\":[{\"caseId\":\"TCGA-OR-A5LN-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"},{\"caseId\":\"TCGA-OR-A5LS-01\",\"molecularProfileId\":\"acc_tcga_pan_can_atlas_2018_rna_seq_v2_mrna\"}],\"name\":\"unaltered\"}]"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(TEST_HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[0].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[0].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[1].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupsStatistics[1].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").value(TEST_HUGO_GENE_SYMBOL_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[0].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[0].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[1].meanExpression")
                        .value(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupsStatistics[1].standardDeviation")
                        .value(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2));
    }
}

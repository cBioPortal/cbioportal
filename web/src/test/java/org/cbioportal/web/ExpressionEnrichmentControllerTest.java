package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.web.parameter.EnrichmentFilter;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
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

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ExpressionEnrichmentService expressionEnrichmentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public ExpressionEnrichmentService expressionEnrichmentService() {
        return Mockito.mock(ExpressionEnrichmentService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(expressionEnrichmentService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void fetchExpressionEnrichments() throws Exception {

        List<ExpressionEnrichment> expressionEnrichments = new ArrayList<>();
        ExpressionEnrichment expressionEnrichment1 = new ExpressionEnrichment();
        expressionEnrichment1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        expressionEnrichment1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        expressionEnrichment1.setCytoband(TEST_CYTOBAND_1);
        expressionEnrichment1.setMeanExpressionInAlteredGroup(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1);
        expressionEnrichment1.setMeanExpressionInUnalteredGroup(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1);
        expressionEnrichment1.setStandardDeviationInAlteredGroup(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1);
        expressionEnrichment1.setStandardDeviationInUnalteredGroup(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1);
        expressionEnrichment1.setpValue(TEST_P_VALUE_1);
        expressionEnrichments.add(expressionEnrichment1);
        ExpressionEnrichment expressionEnrichment2 = new ExpressionEnrichment();
        expressionEnrichment2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        expressionEnrichment2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        expressionEnrichment2.setCytoband(TEST_CYTOBAND_2);
        expressionEnrichment2.setMeanExpressionInAlteredGroup(TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2);
        expressionEnrichment2.setMeanExpressionInUnalteredGroup(TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2);
        expressionEnrichment2.setStandardDeviationInAlteredGroup(TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2);
        expressionEnrichment2.setStandardDeviationInUnalteredGroup(TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2);
        expressionEnrichment2.setpValue(TEST_P_VALUE_2);
        expressionEnrichments.add(expressionEnrichment2);

        Mockito.when(expressionEnrichmentService.getExpressionEnrichments(Mockito.anyString(),
            Mockito.anyListOf(String.class), Mockito.anyListOf(String.class), Mockito.anyString()))
            .thenReturn(expressionEnrichments);

        EnrichmentFilter enrichmentFilter = new EnrichmentFilter();
        enrichmentFilter.setAlteredIds(Arrays.asList("test_sample_id_1"));
        enrichmentFilter.setUnalteredIds(Arrays.asList("test_sample_id_2"));

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/molecular-profiles/test_molecular_profile_id/expression-enrichments/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(enrichmentFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].meanExpressionInAlteredGroup").value(
                TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].meanExpressionInUnalteredGroup").value(
                TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].standardDeviationInAlteredGroup").value(
                TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].standardDeviationInUnalteredGroup").value(
                TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].meanExpressionInAlteredGroup").value(
                TEST_MEAN_EXPRESSION_IN_ALTERED_GROUP_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].meanExpressionInUnalteredGroup").value(
                TEST_MEAN_EXPRESSION_IN_UNALTERED_GROUP_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].standardDeviationInAlteredGroup").value(
                TEST_STANDARD_DEVIATION_IN_ALTERED_GROUP_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].standardDeviationInUnalteredGroup").value(
                TEST_STANDARD_DEVIATION_IN_UNALTERED_GROUP_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2));
    }
}

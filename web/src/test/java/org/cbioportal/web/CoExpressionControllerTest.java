package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CoExpression;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.web.parameter.CoExpressionFilter;
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
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class CoExpressionControllerTest {

    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_1 = new BigDecimal(2.1);
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(0.33);
    private static final BigDecimal TEST_Q_VALUE_1 = new BigDecimal(0.55);
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_2 = new BigDecimal(4.1);
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(0.66);
    private static final BigDecimal TEST_Q_VALUE_2 = new BigDecimal(0.88);

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CoExpressionService coExpressionService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public CoExpressionService coExpressionService() {
        return Mockito.mock(CoExpressionService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(coExpressionService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void fetchCoExpressions() throws Exception {

        List<CoExpression> coExpressionList = new ArrayList<>();
        CoExpression coExpression1 = new CoExpression();
        coExpression1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        coExpression1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        coExpression1.setCytoband(TEST_CYTOBAND_1);
        coExpression1.setSpearmansCorrelation(TEST_SPEARMANS_CORRELATION_1);
        coExpression1.setpValue(TEST_P_VALUE_1);
        coExpression1.setqValue(TEST_Q_VALUE_1);
        coExpressionList.add(coExpression1);
        CoExpression coExpression2 = new CoExpression();
        coExpression2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        coExpression2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        coExpression2.setCytoband(TEST_CYTOBAND_2);
        coExpression2.setSpearmansCorrelation(TEST_SPEARMANS_CORRELATION_2);
        coExpression2.setpValue(TEST_P_VALUE_2);
        coExpression2.setqValue(TEST_Q_VALUE_2);
        coExpressionList.add(coExpression2);

        Mockito.when(coExpressionService.fetchCoExpressions(Mockito.anyString(),
            Mockito.anyListOf(String.class), Mockito.anyInt(), Mockito.anyDouble()))
            .thenReturn(coExpressionList);

        CoExpressionFilter coExpressionFilter = new CoExpressionFilter();
        coExpressionFilter.setSampleIds(Arrays.asList("test_sample_id"));

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/molecular-profiles/test_molecular_profile_id/co-expressions/fetch")
            .param("entrezGeneId", "1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coExpressionFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].spearmansCorrelation").value(TEST_SPEARMANS_CORRELATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].qValue").value(TEST_Q_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].spearmansCorrelation").value(TEST_SPEARMANS_CORRELATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].qValue").value(TEST_Q_VALUE_2));
    }
}

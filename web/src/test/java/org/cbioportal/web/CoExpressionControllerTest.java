package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.EntityType;
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
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class CoExpressionControllerTest {

    private static final String TEST_ENTREZ_GENE_ID_1 = "1";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_1 = new BigDecimal(2.1);
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(0.33);
    private static final String TEST_ENTREZ_GENE_ID_2 = "2";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_2 = new BigDecimal(4.1);
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(0.66);

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CoExpressionService coExpressionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

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
    public void fetchMolecularProfileCoExpressions() throws Exception {

        List<CoExpression> coExpressionList = new ArrayList<>();
        CoExpression coExpression1 = new CoExpression();
        coExpression1.setGeneticEntityId(TEST_ENTREZ_GENE_ID_1);
        coExpression1.setSpearmansCorrelation(TEST_SPEARMANS_CORRELATION_1);
        coExpression1.setpValue(TEST_P_VALUE_1);
        coExpressionList.add(coExpression1);
        CoExpression coExpression2 = new CoExpression();
        coExpression2.setGeneticEntityId(TEST_ENTREZ_GENE_ID_2);
        coExpression2.setSpearmansCorrelation(TEST_SPEARMANS_CORRELATION_2);
        coExpression2.setpValue(TEST_P_VALUE_2);
        coExpressionList.add(coExpression2);


        Mockito.when(coExpressionService.fetchCoExpressions(Mockito.anyString(),
        Mockito.any(EntityType.class), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(), 
        Mockito.anyDouble()))
            .thenReturn(coExpressionList);

        CoExpressionFilter coExpressionFilter = new CoExpressionFilter();
        coExpressionFilter.setSampleIds(Arrays.asList("test_sample_id"));
        coExpressionFilter.setEntrezGeneId(1);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/molecular-profiles/co-expressions/fetch")
            .param("molecularProfileIdA", "test_molecular_profile_id")
            .param("molecularProfileIdB", "test_molecular_profile_id")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coExpressionFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticEntityId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].spearmansCorrelation").value(TEST_SPEARMANS_CORRELATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticEntityId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].spearmansCorrelation").value(TEST_SPEARMANS_CORRELATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2));
    }
}

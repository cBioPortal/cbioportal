package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.EntityType;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.CoExpressionFilter;
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
@ContextConfiguration(classes = {CoExpressionController.class, TestConfig.class})
public class CoExpressionControllerTest {

    private static final String TEST_ENTREZ_GENE_ID_1 = "1";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_1 = new BigDecimal(2.1);
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(0.33);
    private static final String TEST_ENTREZ_GENE_ID_2 = "2";
    private static final BigDecimal TEST_SPEARMANS_CORRELATION_2 = new BigDecimal(4.1);
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(0.66);

    @MockBean
    private CoExpressionService coExpressionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
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
            "/api/molecular-profiles/co-expressions/fetch").with(csrf())
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

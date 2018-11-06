package org.cbioportal.web;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.service.GenesetCorrelationService;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class GenesetCorrelationControllerTest {

    private static final String PROF_ID = "test_prof_id";
    private static final String QUERY_GENESET_ID = "test_geneset_Id";
    private static final int ENTREZ_GENE_ID_1 = 1;
    private static final String HUGO_GENE_SYMBOL_1 = "Hugo1";
    private static final double CORR_1 = 0.5;
    private static final int ENTREZ_GENE_ID_2 = 2;
    private static final String HUGO_GENE_SYMBOL_2 = "Hugo2";
    private static final double CORR_2 = -0.7;
    private static final String EXPR_PROF_ID = "expr_prof_id1";
    private static final String ZSCORE_PROF_ID = "zscore_prof_id1";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GenesetCorrelationService genesetCorrelationService;

    private MockMvc mockMvc;

    @Bean
    public GenesetCorrelationService genesetCorrelationService() {
        return Mockito.mock(GenesetCorrelationService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(genesetCorrelationService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchCorrelatedGenes() throws Exception {

        List<GenesetCorrelation> correlationsForGeneset = createGenesetCorrelationList();
        Mockito.when(genesetCorrelationService.fetchCorrelatedGenes(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyDouble())).thenReturn(correlationsForGeneset);

        mockMvc.perform(MockMvcRequestBuilders.post("/genesets/" + QUERY_GENESET_ID + "/expression-correlation/fetch")
                .param("geneticProfileId", PROF_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].correlationValue").value(CORR_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].expressionGeneticProfileId").value(EXPR_PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].zScoreGeneticProfileId").value(ZSCORE_PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(ENTREZ_GENE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].correlationValue").value(CORR_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].expressionGeneticProfileId").value(EXPR_PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].zScoreGeneticProfileId").value(ZSCORE_PROF_ID));
    }

    private List<GenesetCorrelation> createGenesetCorrelationList() {

        List<GenesetCorrelation> genesetCorrelationList = new ArrayList<>();
        GenesetCorrelation gsc1 = new GenesetCorrelation();
        gsc1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gsc1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        gsc1.setCorrelationValue(CORR_1);
        gsc1.setExpressionMolecularProfileId(EXPR_PROF_ID);
        gsc1.setzScoreMolecularProfileId(ZSCORE_PROF_ID);
        genesetCorrelationList.add(gsc1);
        GenesetCorrelation gsc2 = new GenesetCorrelation();
        gsc2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gsc2.setHugoGeneSymbol(HUGO_GENE_SYMBOL_2);
        gsc2.setCorrelationValue(CORR_2);
        gsc2.setExpressionMolecularProfileId(EXPR_PROF_ID);
        gsc2.setzScoreMolecularProfileId(ZSCORE_PROF_ID);
        genesetCorrelationList.add(gsc2);
        return genesetCorrelationList;
    }
}

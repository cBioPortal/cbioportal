package org.cbioportal.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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
@ContextConfiguration(classes = {SignificantlyMutatedGenesController.class, TestConfig.class})
public class SignificantlyMutatedGenesControllerTest {

    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final int TEST_RANK_1 = 1;
    private static final int TEST_NUMBASESCOVERED_1 = 1;
    private static final int TEST_NUMMUTATIONS_1 = 1;
    private static final BigDecimal TEST_P_VALUE_1 = new BigDecimal(0.1);
    private static final BigDecimal TEST_Q_VALUE_1 = new BigDecimal(0.1);
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final int TEST_RANK_2 = 2;
    private static final int TEST_NUMBASESCOVERED_2 = 2;
    private static final int TEST_NUMMUTATIONS_2 = 2;
    private static final BigDecimal TEST_P_VALUE_2 = new BigDecimal(0.2);
    private static final BigDecimal TEST_Q_VALUE_2 = new BigDecimal(0.2);

    @MockBean
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getSignificantlyMutatedGenesDefaultProjection() throws Exception {

        List<MutSig> mutSigList = new ArrayList<>();
        MutSig mutSig1 = new MutSig();
        mutSig1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        mutSig1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        mutSig1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutSig1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        mutSig1.setRank(TEST_RANK_1);
        mutSig1.setNumbasescovered(TEST_NUMBASESCOVERED_1);
        mutSig1.setNummutations(TEST_NUMMUTATIONS_1);
        mutSig1.setpValue(TEST_P_VALUE_1);
        mutSig1.setqValue(TEST_Q_VALUE_1);
        mutSigList.add(mutSig1);
        MutSig mutSig2 = new MutSig();
        mutSig2.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        mutSig2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        mutSig2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutSig2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        mutSig2.setRank(TEST_RANK_2);
        mutSig2.setNumbasescovered(TEST_NUMBASESCOVERED_2);
        mutSig2.setNummutations(TEST_NUMMUTATIONS_2);
        mutSig2.setpValue(TEST_P_VALUE_2);
        mutSig2.setqValue(TEST_Q_VALUE_2);
        mutSigList.add(mutSig2);

        Mockito.when(significantlyMutatedGeneService.getSignificantlyMutatedGenes(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mutSigList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/significantly-mutated-genes")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numbasescovered").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].nummutations").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfMutations").value(TEST_NUMMUTATIONS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].rank").value(TEST_RANK_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(TEST_P_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].qValue").value(TEST_Q_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numbasescovered").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].nummutations").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfMutations").value(TEST_NUMMUTATIONS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].rank").value(TEST_RANK_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(TEST_P_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].qValue").value(TEST_Q_VALUE_2));
    }

    @Test
    @WithMockUser
    public void getSignificantlyMutatedGenesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(significantlyMutatedGeneService.getMetaSignificantlyMutatedGenes(Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/significantly-mutated-genes")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
}

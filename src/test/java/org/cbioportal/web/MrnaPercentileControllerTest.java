package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.MrnaPercentileService;
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
@ContextConfiguration(classes = {MrnaPercentileController.class, TestConfig.class})
public class MrnaPercentileControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID = "test_molecular_profile_stable_id_1";
    private static final String TEST_SAMPLE_STABLE_ID = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final BigDecimal TEST_Z_SCORE_1 = new BigDecimal(0.1);
    private static final BigDecimal TEST_PERCENTILE_1 = new BigDecimal(50.33);
    private static final BigDecimal TEST_Z_SCORE_2 = new BigDecimal(0.2);
    private static final BigDecimal TEST_PERCENTILE_2 = new BigDecimal(80.01);

    @MockBean
    private MrnaPercentileService mrnaPercentileService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchMrnaPercentile() throws Exception {

        List<MrnaPercentile> mrnaPercentileList = new ArrayList<>();
        MrnaPercentile mrnaPercentile1 = new MrnaPercentile();
        mrnaPercentile1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID);
        mrnaPercentile1.setSampleId(TEST_SAMPLE_STABLE_ID);
        mrnaPercentile1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mrnaPercentile1.setzScore(TEST_Z_SCORE_1);
        mrnaPercentile1.setPercentile(TEST_PERCENTILE_1);
        mrnaPercentileList.add(mrnaPercentile1);
        MrnaPercentile mrnaPercentile2 = new MrnaPercentile();
        mrnaPercentile2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID);
        mrnaPercentile2.setSampleId(TEST_SAMPLE_STABLE_ID);
        mrnaPercentile2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mrnaPercentile2.setzScore(TEST_Z_SCORE_2);
        mrnaPercentile2.setPercentile(TEST_PERCENTILE_2);
        mrnaPercentileList.add(mrnaPercentile2);

        Mockito.when(mrnaPercentileService.fetchMrnaPercentile(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyList())).thenReturn(mrnaPercentileList);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_1);
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_2);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/molecular-profiles/test_molecular_profile_id/mrna-percentile/fetch").with(csrf())
            .param("sampleId", TEST_SAMPLE_STABLE_ID)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entrezGeneIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].zScore").value(TEST_Z_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].percentile").value(TEST_PERCENTILE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].zScore").value(TEST_Z_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].percentile").value(TEST_PERCENTILE_2));
    }
}

package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.MutationPositionIdentifier;
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
@ContextConfiguration(classes = {MutationCountController.class, TestConfig.class})
public class MutationCountControllerTest {

    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_START_1 = 1;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_END_1 = 1;
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_START_2 = 2;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_END_2 = 2;
    private static final int TEST_MUTATION_COUNT_1 = 100;
    private static final int TEST_MUTATION_COUNT_2 = 200;

    @MockBean
    private MutationService mutationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchMutationCountsByPosition() throws Exception {

        List<MutationCountByPosition> mutationCountByPositionList = new ArrayList<>();
        MutationCountByPosition mutationCountByPosition1 = new MutationCountByPosition();
        mutationCountByPosition1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCountByPosition1.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutationCountByPosition1.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutationCountByPosition1.setCount(TEST_MUTATION_COUNT_1);
        mutationCountByPositionList.add(mutationCountByPosition1);
        MutationCountByPosition mutationCountByPosition2 = new MutationCountByPosition();
        mutationCountByPosition2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCountByPosition2.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_2);
        mutationCountByPosition2.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_2);
        mutationCountByPosition2.setCount(TEST_MUTATION_COUNT_2);
        mutationCountByPositionList.add(mutationCountByPosition2);

        Mockito.when(mutationService.fetchMutationCountsByPosition(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyList()))
            .thenReturn(mutationCountByPositionList);

        List<MutationPositionIdentifier> mutationPositionIdentifiers = new ArrayList<>();
        MutationPositionIdentifier mutationPositionIdentifier1 = new MutationPositionIdentifier();
        mutationPositionIdentifier1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationPositionIdentifier1.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutationPositionIdentifier1.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutationPositionIdentifiers.add(mutationPositionIdentifier1);
        MutationPositionIdentifier mutationPositionIdentifier2 = new MutationPositionIdentifier();
        mutationPositionIdentifier2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationPositionIdentifier2.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_2);
        mutationPositionIdentifier2.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_2);
        mutationPositionIdentifiers.add(mutationPositionIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/mutation-counts-by-position/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationPositionIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_MUTATION_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_MUTATION_COUNT_2));
    }

}

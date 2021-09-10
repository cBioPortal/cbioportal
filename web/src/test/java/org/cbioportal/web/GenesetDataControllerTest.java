package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.GenesetDataFilterCriteria;
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
@ContextConfiguration(classes = {GenesetDataController.class, TestConfig.class})
public class GenesetDataControllerTest {

    private static final String PROF_ID = "test_prof_id";
    private static final String SAMPLE_LIST_ID = "test_sample_list_id";
    public static final String GENESET_ID_1 = "geneset_id_1";
    private static final String SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final String VALUE_1 = "0.845";
    public static final String GENESET_ID_2 = "geneset_id_2";
    private static final String VALUE_2 = "-0.457";

    @MockBean
    private GenesetDataService genesetDataService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser
    public void fetchGeneticDataItemsWithSampleIds() throws Exception {
        List<GenesetMolecularData> genesetDataItems = createGenesetDataItemsList();

        String geneticProfileId = PROF_ID;
        List<String> sampleIds = Arrays.asList(SAMPLE_STABLE_ID_1);
        List<String> genesetIds = Arrays.asList(GENESET_ID_1, GENESET_ID_2);

        Mockito.when(genesetDataService.fetchGenesetData(
            geneticProfileId,
            sampleIds,
            genesetIds)
        ).thenReturn(genesetDataItems);

        GenesetDataFilterCriteria genesetDataFilterCriteria = new GenesetDataFilterCriteria();
        genesetDataFilterCriteria.setSampleIds(sampleIds);
        genesetDataFilterCriteria.setGenesetIds(genesetIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/genetic-profiles/" + geneticProfileId + "/geneset-genetic-data/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(genesetDataFilterCriteria)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genesetId").value(GENESET_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genesetId").value(GENESET_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(VALUE_2));
    }

    @Test
    @WithMockUser
    public void fetchGeneticDataItemsWithSampleListId() throws Exception {
        List<GenesetMolecularData> genesetDataItems = createGenesetDataItemsList();
        genesetDataItems.addAll(createGenesetDataItemsList());

        String geneticProfileId = PROF_ID;
        String sampleListId = SAMPLE_LIST_ID;
        List<String> genesetIds = Arrays.asList(GENESET_ID_1, GENESET_ID_2);

        Mockito.when(genesetDataService.fetchGenesetData(
            geneticProfileId,
            sampleListId,
            genesetIds)
        ).thenReturn(genesetDataItems);

        GenesetDataFilterCriteria genesetDataFilterCriteria = new GenesetDataFilterCriteria();
        genesetDataFilterCriteria.setSampleListId(sampleListId);
        genesetDataFilterCriteria.setGenesetIds(genesetIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/genetic-profiles/" + geneticProfileId + "/geneset-genetic-data/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(genesetDataFilterCriteria)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genesetId").value(GENESET_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genesetId").value(GENESET_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesetId").value(GENESET_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].value").value(VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].geneticProfileId").value(geneticProfileId))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesetId").value(GENESET_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].sampleId").value(SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].value").value(VALUE_2));
    }

    private List<GenesetMolecularData> createGenesetDataItemsList() {

        List<GenesetMolecularData> genesetDataItems = new ArrayList<>();
        GenesetMolecularData gsItem1 = new GenesetMolecularData();
        gsItem1.setGenesetId(GENESET_ID_1);
        gsItem1.setMolecularProfileId(PROF_ID);
        gsItem1.setSampleId(SAMPLE_STABLE_ID_1);
        gsItem1.setValue(VALUE_1);
        genesetDataItems.add(gsItem1);
        GenesetMolecularData gsItem2 = new GenesetMolecularData();
        gsItem2.setGenesetId(GENESET_ID_2);
        gsItem2.setMolecularProfileId(PROF_ID);
        gsItem2.setSampleId(SAMPLE_STABLE_ID_1);
        gsItem2.setValue(VALUE_2);
        genesetDataItems.add(gsItem2);
        return genesetDataItems;
    }

}

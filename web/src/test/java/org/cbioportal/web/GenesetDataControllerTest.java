package org.cbioportal.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.web.parameter.GenesetDataFilterCriteria;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class GenesetDataControllerTest {

    private static final String PROF_ID = "test_prof_id";
    private static final String SAMPLE_LIST_ID = "test_sample_list_id";
    public static final String GENESET_ID_1 = "geneset_id_1";
    private static final String SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final String VALUE_1 = "0.845";
    public static final String GENESET_ID_2 = "geneset_id_2";
    private static final String VALUE_2 = "-0.457";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GenesetDataService genesetDataService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public GenesetDataService genesetDataService() {
        return Mockito.mock(GenesetDataService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(genesetDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchGeneticDataItems() throws Exception {

        List<GenesetMolecularData> genesetDataItems = createGenesetDataItemsList();
        Mockito.when(genesetDataService.fetchGenesetData(Mockito.any(), (List) Mockito.any(),
            Mockito.any())).thenReturn(genesetDataItems);

        GenesetDataFilterCriteria genesetDataFilterCriteria = new GenesetDataFilterCriteria();
        genesetDataFilterCriteria.setGenesetIds(Arrays.asList(GENESET_ID_1, GENESET_ID_2));

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/" + PROF_ID + "/geneset-genetic-data/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genesetDataFilterCriteria)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genesetId").value(GENESET_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(SAMPLE_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genesetId").value(GENESET_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(SAMPLE_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(VALUE_2));

        //testing the sampleListId route:
        // List<GenesetMolecularData> genesetDataItems2 = createGenesetDataItemsList();
        // genesetDataItems2.addAll(createGenesetDataItemsList());//duplicate, just to make it different from response above
        // Mockito.when(genesetDataService.fetchGenesetData(Mockito.any(), (List) Mockito.any(),
        //     Mockito.any())).thenReturn(genesetDataItems2);
        // //set sampleListId to ensure the fetchGenesetData variant above is called:
        // genesetDataFilterCriteria.setSampleListId(SAMPLE_LIST_ID);

        // mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/" + PROF_ID + "/geneset-genetic-data/fetch")
        //         .accept(MediaType.APPLICATION_JSON)
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .content(objectMapper.writeValueAsString(genesetDataFilterCriteria)))
        //         .andExpect(MockMvcResultMatchers.status().isOk())
        //         .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        //         .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)));
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

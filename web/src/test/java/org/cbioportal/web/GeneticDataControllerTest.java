package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.web.parameter.GeneticDataFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class GeneticDataControllerTest {

    private static final String TEST_GENETIC_PROFILE_STABLE_ID_1 = "test_genetic_profile_stable_id_1";
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_VALUE_1 = "test_value_1";
    private static final String TEST_GENETIC_PROFILE_STABLE_ID_2 = "test_genetic_profile_stable_id_2";
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_VALUE_2 = "test_value_2";
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GeneticDataService geneticDataService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public GeneticDataService geneticDataService() {
        return Mockito.mock(GeneticDataService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(geneticDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void getAllGeneticDataInGeneticProfileSummaryProjection() throws Exception {

        List<GeneGeneticData> geneGeneticDataList = createExampleGeneticData();

        Mockito.when(geneticDataService.getGeneticData(Mockito.anyString(), 
            Mockito.anyString(), Mockito.anyListOf(Integer.class), Mockito.anyString()))
            .thenReturn(geneGeneticDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id/genetic-data")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("entrezGeneId", "1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void getAllGeneticDataInGeneticProfileMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneticDataService.getMetaGeneticData(Mockito.anyString(), Mockito.anyString(), 
            Mockito.anyListOf(Integer.class))).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id/genetic-data")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("entrezGeneId", "1")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchAllGeneticDataInGeneticProfileSummaryProjection() throws Exception {

        List<GeneGeneticData> geneGeneticDataList = createExampleGeneticData();

        Mockito.when(geneticDataService.fetchGeneticData(Mockito.anyString(),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyString()))
            .thenReturn(geneGeneticDataList);
        
        GeneticDataFilter geneticDataFilter = createGeneticDataFilter();

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/test_genetic_profile_id/genetic-data/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(geneticDataFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void fetchAllGeneticDataInGeneticProfileMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneticDataService.fetchMetaGeneticData(Mockito.anyString(), Mockito.anyListOf(String.class), 
            Mockito.anyListOf(Integer.class))).thenReturn(baseMeta);

        GeneticDataFilter geneticDataFilter = createGeneticDataFilter();

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/test_genetic_profile_id/genetic-data/fetch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(geneticDataFilter))
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
    
    private List<GeneGeneticData> createExampleGeneticData() {
        
        List<GeneGeneticData> geneGeneticDataList = new ArrayList<>();
        GeneGeneticData geneGeneticData1 = new GeneGeneticData();
        geneGeneticData1.setGeneticProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        geneGeneticData1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        geneGeneticData1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        geneGeneticData1.setValue(TEST_VALUE_1);
        geneGeneticDataList.add(geneGeneticData1);
        GeneGeneticData geneGeneticData2 = new GeneGeneticData();
        geneGeneticData2.setGeneticProfileId(TEST_GENETIC_PROFILE_STABLE_ID_2);
        geneGeneticData2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        geneGeneticData2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        geneGeneticData2.setValue(TEST_VALUE_2);
        geneGeneticDataList.add(geneGeneticData2);
        return geneGeneticDataList;
    }

    private GeneticDataFilter createGeneticDataFilter() {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_1);
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_2);

        GeneticDataFilter geneticDataFilter = new GeneticDataFilter();
        geneticDataFilter.setEntrezGeneIds(entrezGeneIds);
        geneticDataFilter.setSampleIds(sampleIds);
        return geneticDataFilter;
    }
}

package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.util.CacheMapUtil;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.web.parameter.MolecularDataFilter;
import org.cbioportal.web.parameter.MolecularDataMultipleStudyFilter;
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
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class MolecularDataControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_1 = "test_molecular_profile_stable_id_1";
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_VALUE_1 = "2.3";
    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_2 = "test_molecular_profile_stable_id_2";
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_VALUE_2 = "2.4";
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MolecularDataService molecularDataService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public MolecularDataService molecularDataService() {
        return Mockito.mock(MolecularDataService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(molecularDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllMolecularDataInMolecularProfileSummaryProjection() throws Exception {

        List<GeneMolecularData> geneMolecularDataList = createExampleMolecularData();

        Mockito.when(molecularDataService.getMolecularData(Mockito.anyString(),
            Mockito.anyString(), Mockito.anyList(), Mockito.anyString()))
            .thenReturn(geneMolecularDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id/molecular-data")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("entrezGeneId", "1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(2.3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(2.4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void getAllMolecularDataInMolecularProfileMetaProjection() throws Exception {

        List<GeneMolecularData> geneMolecularDataList = createExampleMolecularData();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularDataList.add(geneMolecularData1);
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularDataList.add(geneMolecularData2);

        Mockito.when(molecularDataService.getMolecularData(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyList(), Mockito.anyString())).thenReturn(geneMolecularDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id/molecular-data")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("entrezGeneId", "1")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchAllMolecularDataInMolecularProfileSummaryProjection() throws Exception {

        List<GeneMolecularData> geneMolecularDataList = createExampleMolecularData();

        Mockito.when(molecularDataService.fetchMolecularData(Mockito.anyString(),
            Mockito.anyList(), Mockito.anyList(), Mockito.anyString()))
            .thenReturn(geneMolecularDataList);

        MolecularDataFilter molecularDataFilter = createMolecularDataFilter();

        mockMvc.perform(MockMvcRequestBuilders
            .post("/molecular-profiles/test_molecular_profile_id/molecular-data/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(molecularDataFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(2.3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(2.4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void fetchAllMolecularDataInMolecularProfileMetaProjection() throws Exception {

        List<GeneMolecularData> geneMolecularDataList = createExampleMolecularData();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularDataList.add(geneMolecularData1);
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularDataList.add(geneMolecularData2);

        Mockito.when(molecularDataService.fetchMolecularData(Mockito.anyString(), Mockito.anyList(),
            Mockito.anyList(), Mockito.anyString())).thenReturn(geneMolecularDataList);

        MolecularDataFilter molecularDataFilter = createMolecularDataFilter();

        mockMvc.perform(MockMvcRequestBuilders
            .post("/molecular-profiles/test_molecular_profile_id/molecular-data/fetch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(molecularDataFilter))
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchMolecularDataInMultipleMolecularProfiles() throws Exception {

        List<GeneMolecularData> geneMolecularDataList = createExampleMolecularData();

        Mockito.when(molecularDataService.getMolecularDataInMultipleMolecularProfiles(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(geneMolecularDataList);

        MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter = new MolecularDataMultipleStudyFilter();
        molecularDataMultipleStudyFilter.setMolecularProfileIds(Arrays.asList(TEST_MOLECULAR_PROFILE_STABLE_ID_1,
            TEST_MOLECULAR_PROFILE_STABLE_ID_2));
        molecularDataMultipleStudyFilter.setEntrezGeneIds(Arrays.asList(TEST_ENTREZ_GENE_ID_1, TEST_ENTREZ_GENE_ID_2));

        mockMvc.perform(MockMvcRequestBuilders
            .post("/molecular-data/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(molecularDataMultipleStudyFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(2.3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(2.4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    private List<GeneMolecularData> createExampleMolecularData() {

        List<GeneMolecularData> geneMolecularDataList = new ArrayList<>();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularData1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        geneMolecularData1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        geneMolecularData1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        geneMolecularData1.setValue(TEST_VALUE_1);
        geneMolecularDataList.add(geneMolecularData1);
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularData2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_2);
        geneMolecularData2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        geneMolecularData2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        geneMolecularData2.setValue(TEST_VALUE_2);
        geneMolecularDataList.add(geneMolecularData2);
        return geneMolecularDataList;
    }

    private MolecularDataFilter createMolecularDataFilter() {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_1);
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_2);

        MolecularDataFilter molecularDataFilter = new MolecularDataFilter();
        molecularDataFilter.setEntrezGeneIds(entrezGeneIds);
        molecularDataFilter.setSampleIds(sampleIds);
        return molecularDataFilter;
    }
}

package org.cbioportal.web;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.SampleListNotFoundException;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class SampleListControllerTest {

    private static final int TEST_LIST_ID_1 = 1;
    private static final String TEST_STABLE_ID_1 = "test_stable_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_STUDY_IDENTIFIER_1 = "test_study_identifier_1";
    private static final String TEST_CATEGORY_1 = "test_datatype_1";
    private static final String TEST_NAME_1 = "test_name_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_STUDY_NAME_1 = "test_study_name_1";
    private static final String TEST_SHORT_NAME_1 = "test_short_name_1";
    private static final String TEST_STUDY_DESCRIPTION_1 = "test_study_description_1";
    private static final int TEST_SAMPLE_COUNT_1 = 10;
    private static final int TEST_LIST_ID_2 = 2;
    private static final String TEST_STABLE_ID_2 = "test_stable_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_STUDY_IDENTIFIER_2 = "test_study_identifier_2";
    private static final String TEST_CATEGORY_2 = "test_datatype_2";
    private static final String TEST_NAME_2 = "test_name_2";
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SampleListService sampleListService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public SampleListService sampleListService() {
        return Mockito.mock(SampleListService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(sampleListService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllSampleListsDefaultProjection() throws Exception {

        List<SampleList> sampleLists = createExampleSampleLists();

        Mockito.when(sampleListService.getAllSampleLists(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(sampleLists);

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleListId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].category").value(TEST_CATEGORY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2));
    }

    @Test
    public void getAllSampleListsDetailedProjection() throws Exception {

        List<SampleList> sampleLists = new ArrayList<>();
        sampleLists.add(createExampleSampleListWithStudy());

        Mockito.when(sampleListService.getAllSampleLists(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(sampleLists);

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists")
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleCount").value(TEST_SAMPLE_COUNT_1));
    }

    @Test
    public void getAllSampleListsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(sampleListService.getMetaSampleLists()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getSampleListNotFound() throws Exception {

        Mockito.when(sampleListService.getSampleList(Mockito.anyString())).thenThrow(
            new SampleListNotFoundException("test_sample_list_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists/test_sample_list_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Sample list not found: test_sample_list_id"));
    }

    @Test
    public void getSampleList() throws Exception {

        SampleList sampleList = createExampleSampleListWithStudy();

        Mockito.when(sampleListService.getSampleList(Mockito.anyString())).thenReturn(sampleList);

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists/test_sample_list_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleCount").value(TEST_SAMPLE_COUNT_1));
    }

    @Test
    public void getAllSampleListsInStudyDefaultProjection() throws Exception {

        List<SampleList> sampleLists = createExampleSampleLists();

        Mockito.when(sampleListService.getAllSampleListsInStudy(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(sampleLists);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/sample-lists")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleListId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].category").value(TEST_CATEGORY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2));
    }

    @Test
    public void getAllSampleListsInStudyDetailedProjection() throws Exception {

        List<SampleList> sampleLists = new ArrayList<>();
        sampleLists.add(createExampleSampleListWithStudy());

        Mockito.when(sampleListService.getAllSampleListsInStudy(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(sampleLists);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/sample-lists")
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleCount").value(TEST_SAMPLE_COUNT_1));
    }

    @Test
    public void getAllSampleListsInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(sampleListService.getMetaSampleListsInStudy(Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/sample-lists")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getAllSampleIdsInSampleList() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_ID_1);
        sampleIds.add(TEST_SAMPLE_ID_2);

        Mockito.when(sampleListService.getAllSampleIdsInSampleList(Mockito.anyString())).thenReturn(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.get("/sample-lists/test_sample_list_id/sample-ids")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value(TEST_SAMPLE_ID_2));
    }

    @Test
    public void fetchSampleLists() throws Exception {

        List<SampleList> sampleLists = createExampleSampleLists();

        Mockito.when(sampleListService.fetchSampleLists(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(sampleLists);

        mockMvc.perform(MockMvcRequestBuilders.post("/sample-lists/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Arrays.asList(TEST_STABLE_ID_1, TEST_STABLE_ID_2))))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleListId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value(TEST_CATEGORY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].listId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleListId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].category").value(TEST_CATEGORY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2));
    }

    private List<SampleList> createExampleSampleLists() {

        List<SampleList> sampleLists = new ArrayList<>();
        SampleList sampleList1 = new SampleList();
        sampleList1.setListId(TEST_LIST_ID_1);
        sampleList1.setStableId(TEST_STABLE_ID_1);
        sampleList1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        sampleList1.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        sampleList1.setCategory(TEST_CATEGORY_1);
        sampleList1.setName(TEST_NAME_1);
        sampleList1.setDescription(TEST_DESCRIPTION_1);
        sampleLists.add(sampleList1);
        SampleList sampleList2 = new SampleList();
        sampleList2.setListId(TEST_LIST_ID_2);
        sampleList2.setStableId(TEST_STABLE_ID_2);
        sampleList2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        sampleList2.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_2);
        sampleList2.setCategory(TEST_CATEGORY_2);
        sampleList2.setName(TEST_NAME_2);
        sampleList2.setDescription(TEST_DESCRIPTION_2);
        sampleLists.add(sampleList2);
        return sampleLists;
    }

    private SampleList createExampleSampleListWithStudy() {

        SampleList sampleList = new SampleList();
        sampleList.setListId(TEST_LIST_ID_1);
        sampleList.setStableId(TEST_STABLE_ID_1);
        sampleList.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        sampleList.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        sampleList.setCategory(TEST_CATEGORY_1);
        sampleList.setName(TEST_NAME_1);
        sampleList.setDescription(TEST_DESCRIPTION_1);
        sampleList.setSampleCount(TEST_SAMPLE_COUNT_1);
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy.setName(TEST_STUDY_NAME_1);
        cancerStudy.setShortName(TEST_SHORT_NAME_1);
        cancerStudy.setDescription(TEST_STUDY_DESCRIPTION_1);
        sampleList.setCancerStudy(cancerStudy);
        return sampleList;
    }
}

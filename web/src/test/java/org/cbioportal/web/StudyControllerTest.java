package org.cbioportal.web;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class StudyControllerTest {

    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_NAME_1 = "test_name_1";
    private static final String TEST_SHORT_NAME_1 = "test_short_name_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final boolean TEST_PUBLIC_STUDY_1 = true;
    private static final String TEST_PMID_1 = "test_pmid_1";
    private static final String TEST_CITATION_1 = "test_citation_1";
    private static final String TEST_GROUPS_1 = "test_groups_1";
    private static final int TEST_STATUS_1 = 0;
    private static final String TEST_DATE_1 = "2011-12-18 13:17:17";
    private static final String TEST_TAGS_1 = "{\"Analyst\":{\"Name\":\"Jack\",\"Email\":\"jack@something.com\"},\"Load id\":35}";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final String TEST_TYPE_OF_CANCER_ID_2 = "test_type_of_cancer_id_2";
    private static final String TEST_NAME_2 = "test_name_2";
    private static final String TEST_SHORT_NAME_2 = "test_short_name_2";
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final boolean TEST_PUBLIC_STUDY_2 = true;
    private static final String TEST_PMID_2 = "test_pmid_2";
    private static final String TEST_CITATION_2 = "test_citation_2";
    private static final String TEST_GROUPS_2 = "test_groups_2";
    private static final int TEST_STATUS_2 = 0;
    private static final String TEST_DATE_2 = "2013-10-12 11:11:15";
    private static final String TEST_TAGS_2 = "{}";
    private static final String TEST_TYPE_OF_CANCER_NAME = "test_type_of_cancer_name";
    private static final String TEST_CLINICAL_TRIAL_KEYWORDS = "test_clinical_trial_keywords";
    private static final String TEST_DEDICATED_COLOR = "test_dedicated_color";
    private static final String TEST_TYPE_OF_CANCER_SHORT_NAME = "test_type_of_cancer_short_name";
    private static final String TEST_PARENT = "test_parent";
    private static final String TEST_TAGS_3 = "{\"Analyst\":{\"Name\":\"Frank\",\"Email\":\"frank@something.com\"},\"Load id\":43}";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StudyService studyService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public StudyService studyService() {
        return Mockito.mock(StudyService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(studyService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllStudiesDefaultProjection() throws Exception {

        List<CancerStudy> cancerStudyList = createExampleStudies();

        Mockito.when(studyService.getAllStudies(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any())).thenReturn(cancerStudyList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId")
                        .value(TEST_CANCER_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].citation").value(TEST_CITATION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groups").value(TEST_GROUPS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].importDate").value(TEST_DATE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pmid").value(TEST_PMID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].publicStudy").value(TEST_PUBLIC_STUDY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].shortName").value(TEST_SHORT_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(TEST_STATUS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId")
                        .value(TEST_CANCER_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].citation").value(TEST_CITATION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groups").value(TEST_GROUPS_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].importDate").value(TEST_DATE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pmid").value(TEST_PMID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].publicStudy").value(TEST_PUBLIC_STUDY_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].shortName").value(TEST_SHORT_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value(TEST_STATUS_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_2));

    }

    @Test
    public void getAllStudiesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(studyService.getMetaStudies(Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(Mockito.anyString())).thenThrow(new StudyNotFoundException("test_study_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Study not found: test_study_id"));
    }

    @Test
    public void getStudy() throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy.setName(TEST_NAME_1);
        cancerStudy.setShortName(TEST_SHORT_NAME_1);
        cancerStudy.setDescription(TEST_DESCRIPTION_1);
        cancerStudy.setPublicStudy(TEST_PUBLIC_STUDY_1);
        cancerStudy.setPmid(TEST_PMID_1);
        cancerStudy.setCitation(TEST_CITATION_1);
        cancerStudy.setGroups(TEST_GROUPS_1);
        cancerStudy.setStatus(TEST_STATUS_1);
        cancerStudy.setImportDate(simpleDateFormat.parse(TEST_DATE_1));
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        typeOfCancer.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        typeOfCancer.setName(TEST_TYPE_OF_CANCER_NAME);
        typeOfCancer.setClinicalTrialKeywords(TEST_CLINICAL_TRIAL_KEYWORDS);
        typeOfCancer.setDedicatedColor(TEST_DEDICATED_COLOR);
        typeOfCancer.setShortName(TEST_TYPE_OF_CANCER_SHORT_NAME);
        typeOfCancer.setParent(TEST_PARENT);
        cancerStudy.setTypeOfCancer(typeOfCancer);

        Mockito.when(studyService.getStudy(Mockito.anyString())).thenReturn(cancerStudy);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerStudyId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.citation").value(TEST_CITATION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groups").value(TEST_GROUPS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.importDate").value(TEST_DATE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pmid").value(TEST_PMID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.publicStudy").value(TEST_PUBLIC_STUDY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.shortName").value(TEST_SHORT_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(TEST_STATUS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.cancerTypeId")
                        .value(TEST_TYPE_OF_CANCER_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.name").value(TEST_TYPE_OF_CANCER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.clinicalTrialKeywords")
                        .value(TEST_CLINICAL_TRIAL_KEYWORDS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.dedicatedColor").value(TEST_DEDICATED_COLOR))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.shortName")
                        .value(TEST_TYPE_OF_CANCER_SHORT_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerType.parent").value(TEST_PARENT));
    }

    @Test
    public void fetchStudies() throws Exception {

        List<CancerStudy> cancerStudyList = createExampleStudies();

        Mockito.when(studyService.fetchStudies(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(cancerStudyList);

        mockMvc.perform(MockMvcRequestBuilders.post("/studies/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Arrays.asList(TEST_CANCER_STUDY_IDENTIFIER_1,
                TEST_CANCER_STUDY_IDENTIFIER_2))))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId")
                    .value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].citation").value(TEST_CITATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].groups").value(TEST_GROUPS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].importDate").value(TEST_DATE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].pmid").value(TEST_PMID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].publicStudy").value(TEST_PUBLIC_STUDY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].shortName").value(TEST_SHORT_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(TEST_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId")
                    .value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].citation").value(TEST_CITATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].groups").value(TEST_GROUPS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].importDate").value(TEST_DATE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].pmid").value(TEST_PMID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].publicStudy").value(TEST_PUBLIC_STUDY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].shortName").value(TEST_SHORT_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value(TEST_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_2));
    }

    @Test
    public void getTags() throws Exception {

        CancerStudyTags cancerStudyTags = new CancerStudyTags();
        cancerStudyTags.setTags(TEST_TAGS_1);

        Mockito.when(studyService.getTags(Mockito.anyString())).thenReturn(cancerStudyTags);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/tags")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string(TEST_TAGS_1));
    }

    @Test
    public void getEmptyTags() throws Exception {

        Mockito.when(studyService.getTags(Mockito.anyString())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/tags")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string(TEST_TAGS_2));
    }

    @Test
    public void getAllTags() throws Exception {

        List<CancerStudyTags> cancerStudyTagsList = new ArrayList<>();
        CancerStudyTags cancerStudyTags1 = new CancerStudyTags();
        cancerStudyTags1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudyTags1.setTags(TEST_TAGS_1);
        cancerStudyTagsList.add(cancerStudyTags1);

        CancerStudyTags cancerStudyTags2 = new CancerStudyTags();
        cancerStudyTags2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        cancerStudyTags2.setTags(TEST_TAGS_3);
        cancerStudyTagsList.add(cancerStudyTags2);

        Mockito.when(studyService.getTagsForMultipleStudies(Mockito.anyList())).thenReturn(cancerStudyTagsList);

        mockMvc.perform(MockMvcRequestBuilders.post("/studies/tags/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Arrays.asList(TEST_CANCER_STUDY_IDENTIFIER_1,
                TEST_CANCER_STUDY_IDENTIFIER_2))))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId")
                    .value(TEST_CANCER_STUDY_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tags").value(TEST_TAGS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId")
                    .value(TEST_CANCER_STUDY_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tags").value(TEST_TAGS_3));
    }

    private List<CancerStudy> createExampleStudies() throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        List<CancerStudy> cancerStudyList = new ArrayList<>();
        CancerStudy cancerStudy1 = new CancerStudy();
        cancerStudy1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy1.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy1.setName(TEST_NAME_1);
        cancerStudy1.setShortName(TEST_SHORT_NAME_1);
        cancerStudy1.setDescription(TEST_DESCRIPTION_1);
        cancerStudy1.setPublicStudy(TEST_PUBLIC_STUDY_1);
        cancerStudy1.setPmid(TEST_PMID_1);
        cancerStudy1.setCitation(TEST_CITATION_1);
        cancerStudy1.setGroups(TEST_GROUPS_1);
        cancerStudy1.setStatus(TEST_STATUS_1);
        cancerStudy1.setImportDate(simpleDateFormat.parse(TEST_DATE_1));
        cancerStudyList.add(cancerStudy1);
        CancerStudy cancerStudy2 = new CancerStudy();
        cancerStudy2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        cancerStudy2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_2);
        cancerStudy2.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_2);
        cancerStudy2.setName(TEST_NAME_2);
        cancerStudy2.setShortName(TEST_SHORT_NAME_2);
        cancerStudy2.setDescription(TEST_DESCRIPTION_2);
        cancerStudy2.setPublicStudy(TEST_PUBLIC_STUDY_2);
        cancerStudy2.setPmid(TEST_PMID_2);
        cancerStudy2.setCitation(TEST_CITATION_2);
        cancerStudy2.setGroups(TEST_GROUPS_2);
        cancerStudy2.setStatus(TEST_STATUS_2);
        cancerStudy2.setImportDate(simpleDateFormat.parse(TEST_DATE_2));
        cancerStudyList.add(cancerStudy2);
        return cancerStudyList;
    }

}

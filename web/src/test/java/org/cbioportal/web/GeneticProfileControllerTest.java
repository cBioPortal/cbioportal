package org.cbioportal.web;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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
public class GeneticProfileControllerTest {

    private static final int TEST_GENETIC_PROFILE_ID_1 = 1;
    private static final String TEST_STABLE_ID_1 = "test_stable_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_STUDY_IDENTIFIER_1 = "test_study_identifier_1";
    private static final GeneticProfile.GeneticAlterationType TEST_GENETIC_ALTERATION_TYPE_1 =
            GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED;
    private static final String TEST_DATATYPE_1 = "MAF";
    private static final String TEST_NAME_1 = "test_name_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final boolean TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1 = true;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_STUDY_NAME_1 = "test_study_name_1";
    private static final String TEST_SHORT_NAME_1 = "test_short_name_1";
    private static final String TEST_STUDY_DESCRIPTION_1 = "test_study_description_1";
    private static final int TEST_GENETIC_PROFILE_ID_2 = 2;
    private static final String TEST_STABLE_ID_2 = "test_stable_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_STUDY_IDENTIFIER_2 = "test_study_identifier_2";
    private static final GeneticProfile.GeneticAlterationType TEST_GENETIC_ALTERATION_TYPE_2 =
            GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION;
    private static final String TEST_DATATYPE_2 = "CONTINUOUS";
    private static final String TEST_NAME_2 = "test_name_2";
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final boolean TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2 = false;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GeneticProfileService geneticProfileService;
    private MockMvc mockMvc;

    @Bean
    public GeneticProfileService geneticProfileService() {
        return Mockito.mock(GeneticProfileService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(geneticProfileService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllGeneticProfilesDefaultProjection() throws Exception {

        List<GeneticProfile> geneticProfileList = createExampleGeneticProfiles();

        Mockito.when(geneticProfileService.getAllGeneticProfiles(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(geneticProfileList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticAlterationType")
                        .value(TEST_GENETIC_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticAlterationType")
                        .value(TEST_GENETIC_ALTERATION_TYPE_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2));


    }

    @Test
    public void getAllGeneticProfilesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneticProfileService.getMetaGeneticProfiles()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getGeneticProfileNotFound() throws Exception {

        Mockito.when(geneticProfileService.getGeneticProfile(Mockito.anyString())).thenThrow(
                new GeneticProfileNotFoundException("test_genetic_profile_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Genetic profile not found: test_genetic_profile_id"));
    }

    @Test
    public void getGeneticProfile() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticProfileId(TEST_GENETIC_PROFILE_ID_1);
        geneticProfile.setStableId(TEST_STABLE_ID_1);
        geneticProfile.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        geneticProfile.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        geneticProfile.setGeneticAlterationType(TEST_GENETIC_ALTERATION_TYPE_1);
        geneticProfile.setDatatype(TEST_DATATYPE_1);
        geneticProfile.setName(TEST_NAME_1);
        geneticProfile.setDescription(TEST_DESCRIPTION_1);
        geneticProfile.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1);
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy.setName(TEST_STUDY_NAME_1);
        cancerStudy.setShortName(TEST_SHORT_NAME_1);
        cancerStudy.setDescription(TEST_STUDY_DESCRIPTION_1);
        geneticProfile.setCancerStudy(cancerStudy);

        Mockito.when(geneticProfileService.getGeneticProfile(Mockito.anyString())).thenReturn(geneticProfile);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.geneticProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.geneticAlterationType")
                        .value(TEST_GENETIC_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.cancerStudyId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.studyId")
                        .value(TEST_CANCER_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.description").value(TEST_STUDY_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.name").value(TEST_STUDY_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.shortName").value(TEST_SHORT_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.study.cancerTypeId")
                        .value(TEST_TYPE_OF_CANCER_ID_1));
    }

    @Test
    public void getAllGeneticProfilesInStudyDefaultProjection() throws Exception {

        List<GeneticProfile> geneticProfileList = createExampleGeneticProfiles();

        Mockito.when(geneticProfileService.getAllGeneticProfilesInStudy(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(geneticProfileList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/genetic-profiles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticAlterationType")
                        .value(TEST_GENETIC_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticAlterationType")
                        .value(TEST_GENETIC_ALTERATION_TYPE_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2));
    }

    @Test
    public void getAllGeneticProfilesInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneticProfileService.getMetaGeneticProfilesInStudy(Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/genetic-profiles")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<GeneticProfile> createExampleGeneticProfiles() {

        List<GeneticProfile> geneticProfileList = new ArrayList<>();
        GeneticProfile geneticProfile1 = new GeneticProfile();
        geneticProfile1.setGeneticProfileId(TEST_GENETIC_PROFILE_ID_1);
        geneticProfile1.setStableId(TEST_STABLE_ID_1);
        geneticProfile1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        geneticProfile1.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        geneticProfile1.setGeneticAlterationType(TEST_GENETIC_ALTERATION_TYPE_1);
        geneticProfile1.setDatatype(TEST_DATATYPE_1);
        geneticProfile1.setName(TEST_NAME_1);
        geneticProfile1.setDescription(TEST_DESCRIPTION_1);
        geneticProfile1.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1);
        geneticProfileList.add(geneticProfile1);
        GeneticProfile geneticProfile2 = new GeneticProfile();
        geneticProfile2.setGeneticProfileId(TEST_GENETIC_PROFILE_ID_2);
        geneticProfile2.setStableId(TEST_STABLE_ID_2);
        geneticProfile2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        geneticProfile2.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_2);
        geneticProfile2.setGeneticAlterationType(TEST_GENETIC_ALTERATION_TYPE_2);
        geneticProfile2.setDatatype(TEST_DATATYPE_2);
        geneticProfile2.setName(TEST_NAME_2);
        geneticProfile2.setDescription(TEST_DESCRIPTION_2);
        geneticProfile2.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2);
        geneticProfileList.add(geneticProfile2);
        return geneticProfileList;
    }
}

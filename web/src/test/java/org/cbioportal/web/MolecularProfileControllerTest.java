package org.cbioportal.web;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MolecularProfileFilter;
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
public class MolecularProfileControllerTest {

    private static final int TEST_MOLECULAR_PROFILE_ID_1 = 1;
    private static final String TEST_STABLE_ID_1 = "test_stable_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_STUDY_IDENTIFIER_1 = "test_study_identifier_1";
    private static final MolecularProfile.MolecularAlterationType TEST_MOLECULAR_ALTERATION_TYPE_1 =
            MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED;
    private static final String TEST_DATATYPE_1 = "MAF";
    private static final String TEST_NAME_1 = "test_name_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final boolean TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1 = true;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_STUDY_NAME_1 = "test_study_name_1";
    private static final String TEST_SHORT_NAME_1 = "test_short_name_1";
    private static final String TEST_STUDY_DESCRIPTION_1 = "test_study_description_1";
    private static final Float TEST_STUDY_PIVOT_THRESHOLD_1 = 0.1f;
    private static final String TEST_STUDY_SORTORDER_1 = "ASC";
    private static final String TEST_GENERIC_ASSAY_TYPE_1 = "test_generic_assay_type_1";
    
    private static final int TEST_MOLECULAR_PROFILE_ID_2 = 2;
    private static final String TEST_STABLE_ID_2 = "test_stable_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_STUDY_IDENTIFIER_2 = "test_study_identifier_2";
    private static final MolecularProfile.MolecularAlterationType TEST_MOLECULAR_ALTERATION_TYPE_2 =
    MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION;
    private static final String TEST_DATATYPE_2 = "CONTINUOUS";
    private static final String TEST_NAME_2 = "test_name_2";
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final boolean TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2 = false;
    private static final Float TEST_STUDY_PIVOT_THRESHOLD_2 = 0.2f;
    private static final String TEST_STUDY_SORTORDER_2 = "DESC";
    private static final String TEST_GENERIC_ASSAY_TYPE_2 = "test_generic_assay_type_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MolecularProfileService molecularProfileService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public MolecularProfileService molecularProfileService() {
        return Mockito.mock(MolecularProfileService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(molecularProfileService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllMolecularProfilesDefaultProjection() throws Exception {

        List<MolecularProfile> molecularProfileList = createExampleMolecularProfiles();

        Mockito.when(molecularProfileService.getAllMolecularProfiles(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(molecularProfileList);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularAlterationType")
                        .value(TEST_MOLECULAR_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].showProfileInAnalysisTab")
                .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sortOrder").value(TEST_STUDY_SORTORDER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularAlterationType")
                .value(TEST_MOLECULAR_ALTERATION_TYPE_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].showProfileInAnalysisTab")
                .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sortOrder").value(TEST_STUDY_SORTORDER_2));


    }

    @Test
    public void getAllMolecularProfilesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(molecularProfileService.getMetaMolecularProfiles()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenThrow(
                new MolecularProfileNotFoundException("test_molecular_profile_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Molecular profile not found: test_molecular_profile_id"));
    }

    @Test
    public void getMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularProfileId(TEST_MOLECULAR_PROFILE_ID_1);
        molecularProfile.setStableId(TEST_STABLE_ID_1);
        molecularProfile.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        molecularProfile.setMolecularAlterationType(TEST_MOLECULAR_ALTERATION_TYPE_1);
        molecularProfile.setDatatype(TEST_DATATYPE_1);
        molecularProfile.setGenericAssayType(TEST_GENERIC_ASSAY_TYPE_1);
        molecularProfile.setName(TEST_NAME_1);
        molecularProfile.setDescription(TEST_DESCRIPTION_1);
        molecularProfile.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1);
        molecularProfile.setPivotThreshold(TEST_STUDY_PIVOT_THRESHOLD_1);
        molecularProfile.setSortOrder(TEST_STUDY_SORTORDER_1);
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy.setName(TEST_STUDY_NAME_1);
        cancerStudy.setShortName(TEST_SHORT_NAME_1);
        cancerStudy.setDescription(TEST_STUDY_DESCRIPTION_1);
        molecularProfile.setCancerStudy(cancerStudy);

        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.molecularProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.molecularAlterationType")
                        .value(TEST_MOLECULAR_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sortOrder").value(TEST_STUDY_SORTORDER_1))
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
    public void getAllMolecularProfilesInStudyDefaultProjection() throws Exception {

        List<MolecularProfile> molecularProfileList = createExampleMolecularProfiles();

        Mockito.when(molecularProfileService.getAllMolecularProfilesInStudy(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(molecularProfileList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/molecular-profiles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularAlterationType")
                        .value(TEST_MOLECULAR_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sortOrder").value(TEST_STUDY_SORTORDER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularAlterationType")
                        .value(TEST_MOLECULAR_ALTERATION_TYPE_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].showProfileInAnalysisTab")
                        .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sortOrder").value(TEST_STUDY_SORTORDER_2));
    }

    @Test
    public void getAllMolecularProfilesInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(molecularProfileService.getMetaMolecularProfilesInStudy(Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/molecular-profiles")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchMolecularProfiles() throws Exception {

        List<MolecularProfile> molecularProfileList = createExampleMolecularProfiles();

        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(Mockito.anyList(),
            Mockito.anyString())).thenReturn(molecularProfileList);

        MolecularProfileFilter molecularProfileFilter = new MolecularProfileFilter();
        molecularProfileFilter.setStudyIds(Arrays.asList(TEST_STUDY_IDENTIFIER_1, TEST_STUDY_IDENTIFIER_2));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/molecular-profiles/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(molecularProfileFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_IDENTIFIER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularAlterationType")
                .value(TEST_MOLECULAR_ALTERATION_TYPE_1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].showProfileInAnalysisTab")
                .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sortOrder").value(TEST_STUDY_SORTORDER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_IDENTIFIER_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularAlterationType")
                .value(TEST_MOLECULAR_ALTERATION_TYPE_2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericAssayType").value(TEST_GENERIC_ASSAY_TYPE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].showProfileInAnalysisTab")
                .value(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pivotThreshold").value(TEST_STUDY_PIVOT_THRESHOLD_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sortOrder").value(TEST_STUDY_SORTORDER_2));
}

    private List<MolecularProfile> createExampleMolecularProfiles() {

        List<MolecularProfile> molecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_ID_1);
        molecularProfile1.setStableId(TEST_STABLE_ID_1);
        molecularProfile1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        molecularProfile1.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_1);
        molecularProfile1.setMolecularAlterationType(TEST_MOLECULAR_ALTERATION_TYPE_1);
        molecularProfile1.setDatatype(TEST_DATATYPE_1);
        molecularProfile1.setGenericAssayType(TEST_GENERIC_ASSAY_TYPE_1);
        molecularProfile1.setName(TEST_NAME_1);
        molecularProfile1.setDescription(TEST_DESCRIPTION_1);
        molecularProfile1.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_1);
        molecularProfile1.setPivotThreshold(TEST_STUDY_PIVOT_THRESHOLD_1);
        molecularProfile1.setSortOrder(TEST_STUDY_SORTORDER_1);
        molecularProfileList.add(molecularProfile1);

        MolecularProfile molecularProfile2 = new MolecularProfile();
        molecularProfile2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_ID_2);
        molecularProfile2.setStableId(TEST_STABLE_ID_2);
        molecularProfile2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        molecularProfile2.setCancerStudyIdentifier(TEST_STUDY_IDENTIFIER_2);
        molecularProfile2.setMolecularAlterationType(TEST_MOLECULAR_ALTERATION_TYPE_2);
        molecularProfile2.setDatatype(TEST_DATATYPE_2);
        molecularProfile2.setGenericAssayType(TEST_GENERIC_ASSAY_TYPE_2);
        molecularProfile2.setName(TEST_NAME_2);
        molecularProfile2.setDescription(TEST_DESCRIPTION_2);
        molecularProfile2.setShowProfileInAnalysisTab(TEST_SHOW_PROFILE_IN_ANALYSIS_TAB_2);
        molecularProfile2.setPivotThreshold(TEST_STUDY_PIVOT_THRESHOLD_2);
        molecularProfile2.setSortOrder(TEST_STUDY_SORTORDER_2);
        molecularProfileList.add(molecularProfile2);
        return molecularProfileList;
    }
}

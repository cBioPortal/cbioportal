package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
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
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ClinicalAttributeController.class, TestConfig.class})
public class ClinicalAttributeControllerTest {

    private static final String TEST_ATTR_ID_1 = "test_attr_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_DATATYPE_1 = "test_datatype_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final String TEST_DISPLAY_NAME_1 = "test_display_name_1";
    private static final boolean TEST_PATIENT_ATTRIBUTE_1 = true;
    private static final String TEST_PRIORITY_1 = "test_priority_1";
    private static final Integer TEST_ATTRIBUTE_COUNT_1 = 3;
    private static final String TEST_ATTR_ID_2 = "test_attr_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final String TEST_DATATYPE_2 = "test_datatype_2";
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final String TEST_DISPLAY_NAME_2 = "test_display_name_2";
    private static final boolean TEST_PATIENT_ATTRIBUTE_2 = false;
    private static final String TEST_PRIORITY_2 = "test_priority_2";
    private static final Integer TEST_ATTRIBUTE_COUNT_2 = 1;

    @MockBean
    private ClinicalAttributeService clinicalAttributeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllClinicalAttributesDefaultProjection() throws Exception {

        List<ClinicalAttribute> clinicalAttributes = createExampleClinicalAttributes();

        Mockito.when(clinicalAttributeService.getAllClinicalAttributes(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(clinicalAttributes);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clinical-attributes")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].displayName").value(TEST_DISPLAY_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].priority").value(TEST_PRIORITY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].displayName").value(TEST_DISPLAY_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].priority").value(TEST_PRIORITY_2));
    }

    @Test
    @WithMockUser
    public void getAllClinicalAttributesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalAttributeService.getMetaClinicalAttributes()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clinical-attributes")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getAllClinicalAttributesInStudyDefaultProjection() throws Exception {

        List<ClinicalAttribute> clinicalAttributes = createExampleClinicalAttributes();

        Mockito.when(clinicalAttributeService.getAllClinicalAttributesInStudy(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(clinicalAttributes);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-attributes")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].displayName").value(TEST_DISPLAY_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].priority").value(TEST_PRIORITY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].displayName").value(TEST_DISPLAY_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].priority").value(TEST_PRIORITY_2));
    }

    @Test
    @WithMockUser
    public void getAllClinicalAttributesInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalAttributeService.getMetaClinicalAttributesInStudy(Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-attributes")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getClinicalAttributeInStudyNotFound() throws Exception {

        Mockito.when(clinicalAttributeService.getClinicalAttribute(Mockito.anyString(), Mockito.anyString())).thenThrow(
            new ClinicalAttributeNotFoundException("test_study_id", "test_clinical_attribute_id"));

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/studies/test_study_id/clinical-attributes/test_clinical_attribute_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Clinical attribute not found in study test_study_id: test_clinical_attribute_id"));
    }

    @Test
    @WithMockUser
    public void getClinicalAttributeInStudy() throws Exception {

        ClinicalAttribute clinicalAttribute = new ClinicalAttribute();
        clinicalAttribute.setAttrId(TEST_ATTR_ID_1);
        clinicalAttribute.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        clinicalAttribute.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        clinicalAttribute.setDatatype(TEST_DATATYPE_1);
        clinicalAttribute.setDescription(TEST_DESCRIPTION_1);
        clinicalAttribute.setDisplayName(TEST_DISPLAY_NAME_1);
        clinicalAttribute.setPatientAttribute(TEST_PATIENT_ATTRIBUTE_1);
        clinicalAttribute.setPriority(TEST_PRIORITY_1);

        Mockito.when(clinicalAttributeService.getClinicalAttribute(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(clinicalAttribute);

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/studies/test_study_id/clinical-attributes/test_clinical_attribute_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.datatype").value(TEST_DATATYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.displayName").value(TEST_DISPLAY_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientAttribute").value(TEST_PATIENT_ATTRIBUTE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.priority").value(TEST_PRIORITY_1));
    }

    @Test
    @WithMockUser
    public void fetchClinicalAttributes() throws Exception {

        List<ClinicalAttribute> clinicalAttributes = createExampleClinicalAttributes();

        Mockito.when(clinicalAttributeService.fetchClinicalAttributes(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(clinicalAttributes);

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_id_1");
        studyIds.add("study_id_2");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-attributes/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].datatype").value(TEST_DATATYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].displayName").value(TEST_DISPLAY_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].priority").value(TEST_PRIORITY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].datatype").value(TEST_DATATYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].displayName").value(TEST_DISPLAY_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientAttribute").value(TEST_PATIENT_ATTRIBUTE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].priority").value(TEST_PRIORITY_2));
    }

    private List<ClinicalAttribute> createExampleClinicalAttributes() {

        List<ClinicalAttribute> clinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute1 = new ClinicalAttribute();
        clinicalAttribute1.setAttrId(TEST_ATTR_ID_1);
        clinicalAttribute1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        clinicalAttribute1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        clinicalAttribute1.setDatatype(TEST_DATATYPE_1);
        clinicalAttribute1.setDescription(TEST_DESCRIPTION_1);
        clinicalAttribute1.setDisplayName(TEST_DISPLAY_NAME_1);
        clinicalAttribute1.setPatientAttribute(TEST_PATIENT_ATTRIBUTE_1);
        clinicalAttribute1.setPriority(TEST_PRIORITY_1);
        clinicalAttributeList.add(clinicalAttribute1);
        ClinicalAttribute clinicalAttribute2 = new ClinicalAttribute();
        clinicalAttribute2.setAttrId(TEST_ATTR_ID_2);
        clinicalAttribute2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        clinicalAttribute2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_2);
        clinicalAttribute2.setDatatype(TEST_DATATYPE_2);
        clinicalAttribute2.setDescription(TEST_DESCRIPTION_2);
        clinicalAttribute2.setDisplayName(TEST_DISPLAY_NAME_2);
        clinicalAttribute2.setPatientAttribute(TEST_PATIENT_ATTRIBUTE_2);
        clinicalAttribute2.setPriority(TEST_PRIORITY_2);
        clinicalAttributeList.add(clinicalAttribute2);
        return clinicalAttributeList;
    }
}

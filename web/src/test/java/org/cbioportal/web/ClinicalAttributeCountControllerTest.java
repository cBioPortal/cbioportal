package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ClinicalAttributeCountController.class, TestConfig.class})
public class ClinicalAttributeCountControllerTest {

    private static final String TEST_ATTR_ID_1 = "test_attr_id_1";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final Integer TEST_ATTRIBUTE_COUNT_1 = 3;
    private static final String TEST_ATTR_ID_2 = "test_attr_id_2";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final Integer TEST_ATTRIBUTE_COUNT_2 = 1;

    @MockBean
    private ClinicalAttributeService clinicalAttributeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getClinicalAttributeCountsBySampleIds() throws Exception {

        List<ClinicalAttributeCount> clinicalAttributes = new ArrayList<>();
        ClinicalAttributeCount clinicalAttributeCount1 = new ClinicalAttributeCount();
        clinicalAttributeCount1.setAttrId(TEST_ATTR_ID_1);
        clinicalAttributeCount1.setCount(TEST_ATTRIBUTE_COUNT_1);
        clinicalAttributes.add(clinicalAttributeCount1);
        ClinicalAttributeCount clinicalAttributeCount2 = new ClinicalAttributeCount();
        clinicalAttributeCount2.setAttrId(TEST_ATTR_ID_2);
        clinicalAttributeCount2.setCount(TEST_ATTRIBUTE_COUNT_2);
        clinicalAttributes.add(clinicalAttributeCount2);

        Mockito.when(clinicalAttributeService.getClinicalAttributeCountsBySampleIds(Mockito.any(),
            Mockito.any())).thenReturn(clinicalAttributes);

        ClinicalAttributeCountFilter clinicalAttributeCountFilter = new ClinicalAttributeCountFilter();
        List<SampleIdentifier> sampleIdentifierList = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifierList.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(TEST_SAMPLE_ID_2);
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifierList.add(sampleIdentifier2);
        clinicalAttributeCountFilter.setSampleIdentifiers(sampleIdentifierList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-attributes/counts/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalAttributeCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_ATTRIBUTE_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_ATTRIBUTE_COUNT_2));
    }

    @Test
    @WithMockUser
    public void getClinicalAttributeCountsBySampleListId() throws Exception {

        List<ClinicalAttributeCount> clinicalAttributes = new ArrayList<>();
        ClinicalAttributeCount clinicalAttributeCount1 = new ClinicalAttributeCount();
        clinicalAttributeCount1.setAttrId(TEST_ATTR_ID_1);
        clinicalAttributeCount1.setCount(TEST_ATTRIBUTE_COUNT_1);
        clinicalAttributes.add(clinicalAttributeCount1);
        ClinicalAttributeCount clinicalAttributeCount2 = new ClinicalAttributeCount();
        clinicalAttributeCount2.setAttrId(TEST_ATTR_ID_2);
        clinicalAttributeCount2.setCount(TEST_ATTRIBUTE_COUNT_2);
        clinicalAttributes.add(clinicalAttributeCount2);

        Mockito.when(clinicalAttributeService.getClinicalAttributeCountsBySampleListId(Mockito.anyString())).thenReturn(clinicalAttributes);

        ClinicalAttributeCountFilter clinicalAttributeCountFilter = new ClinicalAttributeCountFilter();
        clinicalAttributeCountFilter.setSampleListId("test_sample_list_id");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-attributes/counts/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalAttributeCountFilter))
            .param("projection", "DETAILED"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_ATTRIBUTE_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_ATTRIBUTE_COUNT_2));
    }

}

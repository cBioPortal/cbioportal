package org.cbioportal.legacy.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.service.NamespaceDataService;
import org.cbioportal.legacy.web.config.CustomObjectMapper;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.NamespaceComparisonFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {NamespaceDataController.class, TestConfig.class})
public class NamespaceDataControllerTest {

    private static final String TEST_OUTER_KEY_1 = "test_outer_key_1";
    private static final String TEST_INNER_KEY_1 = "test_inner_key_1";
    private static final String TEST_ATTR_VALUE_1 = "test_attr_value_1";
    private static final String TEST_ATTR_VALUE_2 = "test_attr_value_2";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_OUTER_KEY_2 = "test_outer_key_2";
    private static final String TEST_INNER_KEY_2 = "test_inner_key_2";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";

    @MockBean
    private NamespaceDataService namespaceDataService;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchNamespaceDataForComparison() throws Exception {

        List<NamespaceData> namespaceData = createExampleNamespaceData();

        when(namespaceDataService.fetchNamespaceDataForComparison(
                any(), any(), any(), any())).thenReturn(namespaceData);

        NamespaceComparisonFilter namespaceComparisonFilter = createExampleNamespaceComparisonFilter();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/namespace-data/fetch").with(csrf())
                .param("namespaceDataType", "PATIENT")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(namespaceComparisonFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].outerKey").value(TEST_OUTER_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].innerKey").value(TEST_INNER_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrValue").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].outerKey").value(TEST_OUTER_KEY_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].innerKey").value(TEST_INNER_KEY_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrValue").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").doesNotExist());
    }

    private List<NamespaceData> createExampleNamespaceData() {

        List<NamespaceData> namespaceData = new ArrayList<>();
        NamespaceData namespaceData1 = new NamespaceData();
        namespaceData1.setOuterKey(TEST_OUTER_KEY_1);
        namespaceData1.setInnerKey(TEST_INNER_KEY_1);
        String attrValue1 = TEST_ATTR_VALUE_1;
        namespaceData1.setAttrValue(attrValue1);
        namespaceData.add(namespaceData1);
        NamespaceData namespaceData2 = new NamespaceData();
        namespaceData2.setOuterKey(TEST_OUTER_KEY_2);
        namespaceData2.setInnerKey(TEST_INNER_KEY_2);
        String attrValue2 = TEST_ATTR_VALUE_2;
        namespaceData2.setAttrValue(attrValue2);
        namespaceData.add(namespaceData2);
        return namespaceData;
    }

    private NamespaceComparisonFilter createExampleNamespaceComparisonFilter() {

        NamespaceComparisonFilter namespaceComparisonFilter = new NamespaceComparisonFilter();
        List<SampleIdentifier> sampleIdentifierList = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifierList.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(TEST_SAMPLE_ID_2);
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_2);
        sampleIdentifierList.add(sampleIdentifier2);
        namespaceComparisonFilter.setSampleIdentifiers(sampleIdentifierList);
        NamespaceAttribute namespaceAttribute = new NamespaceAttribute();
        namespaceAttribute.setOuterKey(TEST_OUTER_KEY_1);
        namespaceAttribute.setInnerKey(TEST_INNER_KEY_1);
        namespaceComparisonFilter.setNamespaceAttribute(namespaceAttribute);
        List<String> values = new ArrayList<>();
        values.add(TEST_ATTR_VALUE_1);
        values.add(TEST_ATTR_VALUE_2);
        namespaceComparisonFilter.setValues(values);
        return namespaceComparisonFilter;
    }
}

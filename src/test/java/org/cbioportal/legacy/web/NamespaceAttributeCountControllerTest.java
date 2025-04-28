package org.cbioportal.legacy.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.service.NamespaceAttributeService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.NamespaceAttributeCountFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
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
@ContextConfiguration(classes = {NamespaceAttributeCountController.class, TestConfig.class})
public class NamespaceAttributeCountControllerTest {

    private static final String TEST_OUTER_KEY_1 = "test_outer_key_1";
    private static final String TEST_INNER_KEY_1 = "test_inner_key_1";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final Integer TEST_COUNT_1 = 1;
    private static final String TEST_OUTER_KEY_2 = "test_outer_key_2";
    private static final String TEST_INNER_KEY_2 = "test_inner_key_2";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final Integer TEST_COUNT_2 = 2;

    @MockBean
    private NamespaceAttributeService namespaceAttributeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getNamespaceAttributeCountsBySampleIds() throws Exception {

        List<NamespaceAttributeCount> namespaceAttributeCounts = createExampleNamespaceAttributeCounts();

        Mockito.when(namespaceAttributeService.fetchNamespaceAttributeCountsBySampleIds(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(namespaceAttributeCounts);

        NamespaceAttributeCountFilter namespaceAttributeCountFilter = createExampleNamespaceAttributeCountFilter();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/namespace-attributes/counts/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(namespaceAttributeCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].outerKey").value(TEST_OUTER_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].innerKey").value(TEST_INNER_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].outerKey").value(TEST_OUTER_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].innerKey").value(TEST_INNER_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_COUNT_2));
    }

    private List<NamespaceAttributeCount> createExampleNamespaceAttributeCounts() {

        List<NamespaceAttributeCount> namespaceAttributeCounts = new ArrayList<>();
        NamespaceAttributeCount namespaceAttributeCount1 = new NamespaceAttributeCount();
        namespaceAttributeCount1.setOuterKey(TEST_OUTER_KEY_1);
        namespaceAttributeCount1.setInnerKey(TEST_INNER_KEY_1);
        namespaceAttributeCount1.setCount(TEST_COUNT_1);
        namespaceAttributeCounts.add(namespaceAttributeCount1);
        NamespaceAttributeCount namespaceAttributeCount2 = new NamespaceAttributeCount();
        namespaceAttributeCount2.setOuterKey(TEST_OUTER_KEY_2);
        namespaceAttributeCount2.setInnerKey(TEST_INNER_KEY_2);
        namespaceAttributeCount2.setCount(TEST_COUNT_2);
        namespaceAttributeCounts.add(namespaceAttributeCount2);
        return namespaceAttributeCounts;
    }

    private NamespaceAttributeCountFilter createExampleNamespaceAttributeCountFilter() {

        NamespaceAttributeCountFilter namespaceAttributeCountFilter = new NamespaceAttributeCountFilter();
        List<SampleIdentifier> sampleIdentifierList = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifierList.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(TEST_SAMPLE_ID_2);
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_2);
        sampleIdentifierList.add(sampleIdentifier2);
        namespaceAttributeCountFilter.setSampleIdentifiers(sampleIdentifierList);
        List<NamespaceAttribute> namespaceAttributes = new ArrayList<>();
        NamespaceAttribute namespaceAttribute1 = new NamespaceAttribute();
        namespaceAttribute1.setOuterKey(TEST_OUTER_KEY_1);
        namespaceAttribute1.setInnerKey(TEST_INNER_KEY_1);
        namespaceAttributes.add(namespaceAttribute1);
        NamespaceAttribute namespaceAttribute2 = new NamespaceAttribute();
        namespaceAttribute2.setOuterKey(TEST_OUTER_KEY_2);
        namespaceAttribute2.setInnerKey(TEST_INNER_KEY_2);
        namespaceAttributes.add(namespaceAttribute2);
        namespaceAttributeCountFilter.setNamespaceAttributes(namespaceAttributes);
        return namespaceAttributeCountFilter;
    }
}

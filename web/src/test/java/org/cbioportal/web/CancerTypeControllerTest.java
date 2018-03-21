package org.cbioportal.web;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
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
public class CancerTypeControllerTest {

    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_NAME_1 = "test_type_of_cancer_name_1";
    private static final String TEST_CLINICAL_TRIAL_KEYWORDS_1 = "test_clinical_trial_keywords_1";
    private static final String TEST_DEDICATED_COLOR_1 = "test_dedicated_color_1";
    private static final String TEST_SHORT_NAME_1 = "test_type_of_cancer_short_name_1";
    private static final String TEST_PARENT_1 = "test_parent_1";
    private static final String TEST_TYPE_OF_CANCER_ID_2 = "test_type_of_cancer_id_2";
    private static final String TEST_NAME_2 = "test_type_of_cancer_name_2";
    private static final String TEST_CLINICAL_TRIAL_KEYWORDS_2 = "test_clinical_trial_keywords_2";
    private static final String TEST_DEDICATED_COLOR_2 = "test_dedicated_color_2";
    private static final String TEST_SHORT_NAME_2 = "test_type_of_cancer_short_name_2";
    private static final String TEST_PARENT_2 = "test_parent_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CancerTypeService cancerTypeService;
    private MockMvc mockMvc;

    @Bean
    public CancerTypeService cancerTypeService() {
        return Mockito.mock(CancerTypeService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(cancerTypeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllCancerTypesDefaultProjection() throws Exception {

        List<TypeOfCancer> typeOfCancerList = new ArrayList<>();
        TypeOfCancer typeOfCancer1 = new TypeOfCancer();
        typeOfCancer1.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        typeOfCancer1.setName(TEST_NAME_1);
        typeOfCancer1.setClinicalTrialKeywords(TEST_CLINICAL_TRIAL_KEYWORDS_1);
        typeOfCancer1.setDedicatedColor(TEST_DEDICATED_COLOR_1);
        typeOfCancer1.setShortName(TEST_SHORT_NAME_1);
        typeOfCancer1.setParent(TEST_PARENT_1);
        typeOfCancerList.add(typeOfCancer1);
        TypeOfCancer typeOfCancer2 = new TypeOfCancer();
        typeOfCancer2.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_2);
        typeOfCancer2.setName(TEST_NAME_2);
        typeOfCancer2.setClinicalTrialKeywords(TEST_CLINICAL_TRIAL_KEYWORDS_2);
        typeOfCancer2.setDedicatedColor(TEST_DEDICATED_COLOR_2);
        typeOfCancer2.setShortName(TEST_SHORT_NAME_2);
        typeOfCancer2.setParent(TEST_PARENT_2);
        typeOfCancerList.add(typeOfCancer2);

        Mockito.when(cancerTypeService.getAllCancerTypes(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(typeOfCancerList);

        mockMvc.perform(MockMvcRequestBuilders.get("/cancer-types")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalTrialKeywords")
                        .value(TEST_CLINICAL_TRIAL_KEYWORDS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].dedicatedColor").value(TEST_DEDICATED_COLOR_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].shortName").value(TEST_SHORT_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].parent").value(TEST_PARENT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TEST_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalTrialKeywords")
                        .value(TEST_CLINICAL_TRIAL_KEYWORDS_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].dedicatedColor").value(TEST_DEDICATED_COLOR_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].shortName").value(TEST_SHORT_NAME_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].parent").value(TEST_PARENT_2));
    }

    @Test
    public void getAllCancerTypesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(cancerTypeService.getMetaCancerTypes()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/cancer-types")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getCancerTypeNotFound() throws Exception {

        Mockito.when(cancerTypeService.getCancerType(Mockito.anyString()))
                .thenThrow(new CancerTypeNotFoundException("cancer_type_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/cancer-types/cancer_type_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Cancer type not found: cancer_type_id"));
    }

    @Test
    public void getCancerType() throws Exception {

        TypeOfCancer typeOfCancer = new TypeOfCancer();
        typeOfCancer.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        typeOfCancer.setName(TEST_NAME_1);
        typeOfCancer.setClinicalTrialKeywords(TEST_CLINICAL_TRIAL_KEYWORDS_1);
        typeOfCancer.setDedicatedColor(TEST_DEDICATED_COLOR_1);
        typeOfCancer.setShortName(TEST_SHORT_NAME_1);
        typeOfCancer.setParent(TEST_PARENT_1);

        Mockito.when(cancerTypeService.getCancerType(Mockito.anyString())).thenReturn(typeOfCancer);

        mockMvc.perform(MockMvcRequestBuilders.get("/cancer-types/cancer_type_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cancerTypeId").value(TEST_TYPE_OF_CANCER_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TEST_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.clinicalTrialKeywords")
                        .value(TEST_CLINICAL_TRIAL_KEYWORDS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dedicatedColor").value(TEST_DEDICATED_COLOR_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.shortName").value(TEST_SHORT_NAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.parent").value(TEST_PARENT_1));
    }
}
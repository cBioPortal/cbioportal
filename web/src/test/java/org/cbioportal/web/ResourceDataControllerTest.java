package org.cbioportal.web;

import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ResourceData;
import org.cbioportal.service.ResourceDataService;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class ResourceDataControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ResourceDataService resourceDataService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public ResourceDataService resourceDataService() {
        return Mockito.mock(ResourceDataService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(resourceDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllResourceDataOfSampleInStudy() throws Exception {

        String resourceDatasJson = "[" + 
                "  {" + 
                "    \"sampleId\": \"TCGA-OR-A5J1-01\"," + 
                "    \"patientId\": \"TCGA-OR-A5J1\"," + 
                "    \"studyId\": \"test_study_id\"," + 
                "    \"resourceId\": \"PATHOLOGY_SLIDE\"," + 
                "    \"url\": \"http://url-to-slide-sample1\"" + 
                "  }" + 
                "]";

        List<ResourceData> resourceDataList = Arrays
                .asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

        Mockito.when(resourceDataService.getAllResourceDataOfSampleInStudy(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(resourceDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples/test_sample_id/resource-data")
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("TCGA-OR-A5J1-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("TCGA-OR-A5J1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("PATHOLOGY_SLIDE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-slide-sample1"));
    }

    @Test
    public void getAllResourceDataOfPatientInStudy() throws Exception {

        String resourceDatasJson = "[" + 
                "  {" + 
                "    \"patientId\": \"TCGA-OR-A5J1\"," + 
                "    \"studyId\": \"test_study_id\"," + 
                "    \"resourceId\": \"PATIENT_NOTES\"," + 
                "    \"url\": \"http://url-to-notes-patient1.txt\"" + 
                "  }" + 
                "]";

        List<ResourceData> resourceDataList = Arrays
                .asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

        Mockito.when(resourceDataService.getAllResourceDataOfPatientInStudy(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(resourceDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/patients/test_sample_id/resource-data")
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("TCGA-OR-A5J1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("PATIENT_NOTES"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-notes-patient1.txt"));
    }

    @Test
    public void getAllClinicalDataInStudy() throws Exception {

        String resourceDatasJson = "[" + 
                "  {" + 
                "    \"studyId\": \"test_study_id\"," + 
                "    \"resourceId\": \"STUDY_SPONSORS\"," + 
                "    \"url\": \"http://url-to-sponsors.txt\"" + 
                "  }" + 
                "]";

        List<ResourceData> resourceDataList = Arrays
                .asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

        Mockito.when(resourceDataService.getAllResourceDataForStudy(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(resourceDataList);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/studies/test_study_id/resource-data").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("STUDY_SPONSORS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-sponsors.txt"));
    }

}

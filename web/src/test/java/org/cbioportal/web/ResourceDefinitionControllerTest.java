package org.cbioportal.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.service.ResourceDefinitionService;
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
public class ResourceDefinitionControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ResourceDefinitionService resourceDefinitionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public ResourceDefinitionService resourceDefinitionService() {
        return Mockito.mock(ResourceDefinitionService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(resourceDefinitionService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllResourceDefinitionsInStudy() throws Exception {

        String resourceDefinitionsJson = "[" + 
                "  {" + 
                "    \"resourceId\": \"PATHOLOGY_SLIDE\"," + 
                "    \"displayName\": \"Pathology Slide\"," + 
                "    \"description\": \"The pathology slide for the sample\"," + 
                "    \"resourceType\": \"SAMPLE\"," + 
                "    \"priority\": \"1\"," + 
                "    \"openByDefault\": true," + 
                "    \"cancerStudyIdentifier\": \"test_study_id\"" + 
                "  }," + 
                "  {" + 
                "    \"resourceId\": \"PATIENT_NOTES\"," + 
                "    \"displayName\": \"Patient Notes\"," + 
                "    \"description\": \"Notes about the patient\"," + 
                "    \"resourceType\": \"PATIENT\"," + 
                "    \"priority\": \"2\"," + 
                "    \"openByDefault\": false," + 
                "    \"cancerStudyIdentifier\": \"test_study_id\"" + 
                "  }," + 
                "  {" + 
                "    \"resourceId\": \"STUDY_SPONSORS\"," + 
                "    \"displayName\": \"Study Sponsors\"," + 
                "    \"description\": \"Sponsors of this study\"," + 
                "    \"resourceType\": \"STUDY\"," + 
                "    \"priority\": \"3\"," + 
                "    \"openByDefault\": true," + 
                "    \"cancerStudyIdentifier\": \"test_study_id\"" + 
                "  }" + 
                "]";

        List<ResourceDefinition> resourceDefinitions = Arrays
                .asList(objectMapper.readValue(resourceDefinitionsJson, ResourceDefinition[].class));

        Mockito.when(resourceDefinitionService.getAllResourceDefinitionsInStudy(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(resourceDefinitions);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/resource-definitions")
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("PATHOLOGY_SLIDE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceType").value("SAMPLE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].resourceId").value("PATIENT_NOTES"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].resourceType").value("PATIENT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].resourceId").value("STUDY_SPONSORS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].resourceType").value("STUDY"));
        ;
    }

    @Test
    public void getResourceDefinitionInStudy() throws Exception {

        String resourceDefinition = "{" + 
                "    \"resourceId\": \"PATHOLOGY_SLIDE\"," + 
                "    \"displayName\": \"Pathology Slide\"," + 
                "    \"description\": \"The pathology slide for the sample\"," + 
                "    \"resourceType\": \"SAMPLE\"," + 
                "    \"priority\": \"1\"," + 
                "    \"openByDefault\": true," + 
                "    \"cancerStudyIdentifier\": \"test_study_id\"" + 
                "  }";

        ResourceDefinition resourceDefinitions = objectMapper.readValue(resourceDefinition, ResourceDefinition.class);

        Mockito.when(resourceDefinitionService.getResourceDefinition(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(resourceDefinitions);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/resource-definitions/PATHOLOGY_SLIDE")
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("resourceId").value("PATHOLOGY_SLIDE"))
                .andExpect(MockMvcResultMatchers.jsonPath("studyId").value("test_study_id"))
                .andExpect(MockMvcResultMatchers.jsonPath("resourceType").value("SAMPLE"));
        ;
    }

}

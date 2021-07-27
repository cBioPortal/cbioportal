package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.service.AlterationDriverAnnotationService;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class AlterationDriverAnnotationControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AlterationDriverAnnotationService alterationDriverAnnotationService;

    private MockMvc mockMvc;

    @Bean
    public AlterationDriverAnnotationService alterationDriverAnnotationService() {
        return Mockito.mock(AlterationDriverAnnotationService.class);
    }

    @Before
    public void setUp() throws Exception {
        Mockito.reset(alterationDriverAnnotationService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchCustomDriverAnnotationReport() throws Exception {
        CustomDriverAnnotationReport report = new CustomDriverAnnotationReport(true, new HashSet<>(Arrays.asList("a", "b")));
        when(alterationDriverAnnotationService.getCustomDriverAnnotationProps(
            anyList()))
            .thenReturn(report);

        Map<String, Object> body = new HashMap<>();
        body.put("molecularProfileIds", Arrays.asList("molecularProfileId1"));
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/custom-driver-annotation-report/fetch")
            .param("molecularProfileIds", "molecularProfileId1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasBinary", Matchers.equalTo(true)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.tiers", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.tiers[0]", Matchers.equalTo("a")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.tiers[1]", Matchers.equalTo("b")));
    }
}

package org.cbioportal.web;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.service.AlterationDriverAnnotationService;
import org.cbioportal.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Before;
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
@WebMvcTest(AlterationDriverAnnotationController.class)
@ContextConfiguration(classes={AlterationDriverAnnotationController.class, TestConfig.class})
public class AlterationDriverAnnotationControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    
    @MockBean
    private AlterationDriverAnnotationService alterationDriverAnnotationService;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        Mockito.reset(alterationDriverAnnotationService);
    }

    @Test
    @WithMockUser
    public void fetchCustomDriverAnnotationReport() throws Exception {
        CustomDriverAnnotationReport report = new CustomDriverAnnotationReport(true, new HashSet<>(Arrays.asList("a", "b")));
        when(alterationDriverAnnotationService.getCustomDriverAnnotationProps(
            anyList()))
            .thenReturn(report);

        List<String> body = new ArrayList<>(Collections.singletonList("molecularProfileId1"));
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/custom-driver-annotation-report/fetch").with(csrf())
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

package org.cbioportal.web;

import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.removeme.Session;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.util.StudyViewFilterApplier;
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

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {SessionServiceController.class, TestConfig.class})
public class SessionServiceControllerTest {

    @MockBean
    SessionServiceRequestHandler sessionServiceRequestHandler;

    @MockBean
    StudyViewFilterApplier studyViewFilterApplier;

    SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
    { sampleIdentifier1.setStudyId("STUDY_1"); }
    SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
    { sampleIdentifier2.setStudyId("STUDY_2"); }

    @Autowired
    private MockMvc mockMvc;
    @Test
    @WithMockUser
    public void testStaticVirtualStudy() throws Exception {
        Mockito.when(sessionServiceRequestHandler.getSessionDataJson(Session.SessionType.virtual_study, "123"))
            .thenReturn("""
                {
                    "id": "123",
                    "data": {
                        "name": "Test",
                        "studies": [
                            { "id": "STUDY_N", "samples": [ "S1", "S2" ] }
                        ]
                    }
                }
                """);

        // Should not be used
        Mockito.when(studyViewFilterApplier.apply(Mockito.any())).thenReturn(List.of(sampleIdentifier1, sampleIdentifier2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/session/virtual_study/123")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.studies", Matchers.hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.studies[0].id").value("STUDY_N"));
        Mockito.verify(studyViewFilterApplier, Mockito.never()).apply(Mockito.any());
    }

    @Test
    @WithMockUser 
    public void testDynamicVirtualStudy() throws Exception {
        Mockito.when(sessionServiceRequestHandler.getSessionDataJson(Session.SessionType.virtual_study, "123"))
            .thenReturn("""
                {
                    "id": "123",
                    "data": {
                        "name": "Test",
                        "dynamic": true
                    }
                }
                """);

        Mockito.when(studyViewFilterApplier.apply(Mockito.any())).thenReturn(List.of(sampleIdentifier1, sampleIdentifier2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/session/virtual_study/123")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.studies", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.studies[*].id").value(Matchers.containsInAnyOrder("STUDY_1", "STUDY_2")));
    }
}
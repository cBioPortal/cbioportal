package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
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
@ContextConfiguration(classes = {TreatmentController.class, TestConfig.class})
public class TreatmentControllerTest {

    private final List<SampleIdentifier> sampleIdentifiers;
    private final List<String> studies;
    private final StudyViewFilter studyViewFilter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private StudyViewFilterApplier studyViewFilterApplier;

    @MockBean
    private StudyViewFilterUtil studyViewFilterUtil;
    
    @MockBean
    private TreatmentService treatmentService;
    
    @Autowired
    private MockMvc mockMvc;
    
    public TreatmentControllerTest() {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        String STUDY_A = "study_0";
        sampleIdentifier.setStudyId(STUDY_A);
        String SAMPLE_A = "sample_0";
        sampleIdentifier.setSampleId(SAMPLE_A);

        SampleIdentifier sampleIdentifierB = new SampleIdentifier();
        String STUDY_B = "study_1";
        sampleIdentifier.setStudyId(STUDY_B);
        String SAMPLE_B = "sample_1";
        sampleIdentifier.setSampleId(SAMPLE_B);
        sampleIdentifiers = Arrays.asList(sampleIdentifier, sampleIdentifierB);

        studies = Arrays.asList(STUDY_A, STUDY_B);
        
        studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(studies);
    }
    
    @Test
    @WithMockUser
    public void getAllPatientTreatments() throws Exception {
        Mockito
            .when(studyViewFilterApplier.apply(Mockito.any()))
            .thenReturn(sampleIdentifiers);
        
        PatientTreatmentRow rowA = new PatientTreatmentRow("madeupanib", 2, null);
        PatientTreatmentRow rowB = new PatientTreatmentRow("fakeazil", 4, null);
        List<PatientTreatmentRow> treatmentRows = Arrays.asList(rowA, rowB);

        Mockito
            .when(treatmentService.getAllPatientTreatmentRows(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
            .thenReturn(treatmentRows);
        
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/treatments/patient").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatment").value("madeupanib"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].treatment").value("fakeazil"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value("4"));
    }

    @Test
    @WithMockUser
    public void getAllSampleTreatments() throws Exception {
        Mockito
            .when(studyViewFilterApplier.apply(Mockito.any()))
            .thenReturn(sampleIdentifiers);

        SampleTreatmentRow rowA = new SampleTreatmentRow(TemporalRelation.Pre, "madeupanib", 2, null);
        SampleTreatmentRow rowB = new SampleTreatmentRow(TemporalRelation.Post, "madeupanib", 4, null);
        SampleTreatmentRow rowC = new SampleTreatmentRow(TemporalRelation.Pre, "fakeazil", 4, null);
        SampleTreatmentRow rowD = new SampleTreatmentRow(TemporalRelation.Post, "fakeazil", 2, null);
        List<SampleTreatmentRow> sampleTreatmentRows = Arrays.asList(rowA, rowB, rowC, rowD);

        Mockito
            .when(treatmentService.getAllSampleTreatmentRows(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
            .thenReturn(sampleTreatmentRows);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/treatments/sample").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatment").value("madeupanib"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].time").value("Pre"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].treatment").value("madeupanib"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value("4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].time").value("Post"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].treatment").value("fakeazil"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].count").value("4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].time").value("Pre"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].treatment").value("fakeazil"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].count").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].time").value("Post"));
    }

    @Test
    @WithMockUser
    public void getContainsTreatmentData() throws Exception {
        List<String> studies = Arrays.asList("study_0", "study_1");
        Mockito
            .when(studyViewFilterApplier.apply(Mockito.any()))
            .thenReturn(sampleIdentifiers);

        Mockito
            .when(treatmentService.containsTreatmentData(Mockito.anyList(), Mockito.any()))
            .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/treatments/display-patient").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studies)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("true"));
    }


}
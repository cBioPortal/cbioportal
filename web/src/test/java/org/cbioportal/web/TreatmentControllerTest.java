package org.cbioportal.web;

import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TreatmentControllerTest {
    private final List<SampleIdentifier> sampleIdentifiers;
    private final List<String> studies;
    private final StudyViewFilter studyViewFilter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    
    @Autowired
    private TreatmentService treatmentService;
    
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(treatmentService);
        Mockito.reset(studyViewFilterApplier);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

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
    public void getAllPatientTreatments() throws Exception {
        Mockito
            .when(studyViewFilterApplier.apply(Mockito.any()))
            .thenReturn(sampleIdentifiers);
        
        PatientTreatmentRow rowA = new PatientTreatmentRow("madeupanib", 2, null);
        PatientTreatmentRow rowB = new PatientTreatmentRow("fakeazil", 4, null);
        List<PatientTreatmentRow> treatmentRows = Arrays.asList(rowA, rowB);

        Mockito
            .when(treatmentService.getAllPatientTreatmentRows(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(treatmentRows);
        
        mockMvc.perform(MockMvcRequestBuilders.post(
            "/treatments/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatment").value("madeupanib"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].treatment").value("fakeazil"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value("4"));
    }

    @Test
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
            .when(treatmentService.getAllSampleTreatmentRows(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(sampleTreatmentRows);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/treatments/sample")
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
    public void getContainsTreatmentData() throws Exception {
        Mockito
            .when(studyViewFilterApplier.apply(Mockito.any()))
            .thenReturn(sampleIdentifiers);

        Mockito
            .when(treatmentService.containsTreatmentData(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/treatments/display")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("true"));
    }


}
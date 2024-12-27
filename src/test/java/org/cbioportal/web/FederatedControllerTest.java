package org.cbioportal.web;


import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.impl.ClinicalAttributeServiceImpl;
import org.cbioportal.service.impl.ClinicalDataServiceImpl;
import org.cbioportal.service.impl.FederatedViewServiceImpl;
import org.cbioportal.web.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {
    FederatedController.class,
    FederatedViewServiceImpl.class,
    ClinicalDataServiceImpl.class,
    ClinicalAttributeServiceImpl.class,
    StudyViewFilterUtil.class,
    StudyViewFilterApplier.class,
    ClinicalDataBinUtil.class
})
public class FederatedControllerTest {

    private final String STUDY_ID = "test_study_1";
    private final String STUDY_ID_2 = "test_study_2";

    private final String PATIENT_ID_1 = "patient_id_1";
    private final String PATIENT_ID_2 = "patient_id_2";
    private final String PATIENT_ID_3 = "patient_id_3";

    private final String SAMPLE_ID1 = "sample_id_1";
    private final String SAMPLE_ID2 = "sample_id_2";
    private final String SAMPLE_ID3 = "sample_id_3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FederatedService federatedService;

    @MockBean
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @MockBean
    private SampleRepository sampleRepository;

    @MockBean
    private ClinicalDataRepository clinicalDataRepository;


    private List<ClinicalAttribute> mockClinicalAttributes() {

        var attrib11 = new ClinicalAttribute();
        attrib11.setDisplayName("Sex");
        attrib11.setDescription("Sex");
        attrib11.setDatatype("STRING");
        attrib11.setPatientAttribute(false);
        attrib11.setPriority("1");
        attrib11.setAttrId("SEX");
        attrib11.setCancerStudyIdentifier(STUDY_ID);

        var attrib21 = new ClinicalAttribute();
        attrib21.setDisplayName("Age");
        attrib21.setDescription("Age");
        attrib21.setDatatype("NUMBER");
        attrib21.setPatientAttribute(false);
        attrib21.setPriority("1");
        attrib21.setAttrId("AGE");
        attrib21.setCancerStudyIdentifier(STUDY_ID);

        var attrib12 = new ClinicalAttribute();
        attrib12.setDisplayName("Sex");
        attrib12.setDescription("Sex");
        attrib12.setDatatype("STRING");
        attrib12.setPatientAttribute(false);
        attrib12.setPriority("1");
        attrib12.setAttrId("SEX");
        attrib12.setCancerStudyIdentifier(STUDY_ID_2);

        var attrib22 = new ClinicalAttribute();
        attrib22.setDisplayName("Age");
        attrib22.setDescription("Age");
        attrib22.setDatatype("NUMBER");
        attrib22.setPatientAttribute(false);
        attrib22.setPriority("1");
        attrib22.setAttrId("AGE");
        attrib22.setCancerStudyIdentifier(STUDY_ID_2);

        return List.of(attrib11, attrib21, attrib12, attrib22);
    }

    private List<Sample> mockSamples() {
        var sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(STUDY_ID);

        var sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID_2);
        sample2.setCancerStudyIdentifier(STUDY_ID);

        var sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setPatientStableId(PATIENT_ID_3);
        sample3.setCancerStudyIdentifier(STUDY_ID_2);

        return List.of(sample1, sample2, sample3);
    }

    @Before
    public void setup() {
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        List<Sample> samples = mockSamples();

        when(
            clinicalAttributeRepository.fetchClinicalAttributes(
                /* studyIds */ any(),
                /* projection */ any()
            )
        ).thenReturn(
            attribs
        );

        // Methods used by fetchClinicalDataCounts

        // When cross-checking list of attributes requested
        when(
            clinicalAttributeRepository.getClinicalAttributesByStudyIdsAndAttributeIds(
                /* studyIds */ any(),
                /* attributeIds */ any()
            )
        ).thenReturn(
            attribs
        );

        // When applying the study view filter to get a list of samples
        when(
            sampleRepository.fetchSamples(
                /* studyIds */ any(),
                /* sampleIds */ any(),
                /* projection */ any()
            )
        ).thenReturn(
            samples
        );

//        // When tallying the counts
//        when(
//            clinicalDataRepository.fetchClinicalDataCounts(
//                /* studyIds */ any(),
//                /* sampleIds */ any(),
//                /* attributeIds */ any(),
//                /* clinicalDataType */ any(),
//                /* projection */ any()
//            )
//        ).thenReturn(
//
//        );
//
//        // Methods used by fetchClinicalDataBinCounts
//        when(
//            clinicalDataRepository.fetchClinicalData(
//                /* studyIds */ any(),
//                /* sampleIds */ any(),
//                /* attributeIds */ any(),
//                /* clinicalDataType */ any(),
//                /* projection */ any()
//            )
//        );
    }

    @Test
    @WithMockUser
    public void fetchClinicalAttributes() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api-fed/clinical-attributes/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].displayName").value("Sex"))
            .andExpect(jsonPath("$[0].description").value("Sex"))
            .andExpect(jsonPath("$[0].datatype").value("STRING"))
            .andExpect(jsonPath("$[0].patientAttribute").value(true))
            .andExpect(jsonPath("$[0].priority").value("1"))
            .andExpect(jsonPath("$[0].clinicalAttributeId").value("SEX"))
            .andExpect(jsonPath("$[0].studyId").value(STUDY_ID))
            .andExpect(jsonPath("$[1].displayName").value("Age"))
            .andExpect(jsonPath("$[1].description").value("Age"))
            .andExpect(jsonPath("$[1].datatype").value("NUMBER"))
            .andExpect(jsonPath("$[1].patientAttribute").value(true))
            .andExpect(jsonPath("$[1].priority").value("1"))
            .andExpect(jsonPath("$[1].clinicalAttributeId").value("AGE"))
            .andExpect(jsonPath("$[1].studyId").value(STUDY_ID));
    }
}

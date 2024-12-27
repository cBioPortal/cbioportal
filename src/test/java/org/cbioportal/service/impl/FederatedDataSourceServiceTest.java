package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.ClinicalDataFetcher;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FederatedDataSourceServiceTest extends BaseServiceImplTest {
    
    // NOTE: see ClinicalDataBinUtilTest for another example of a class where
    // we are mocking out deeply nested dependencies, rather than direct subfields
    // of the class under test.
    
    @Spy
    @InjectMocks
    private FederatedDataSourceService federatedDataSourceService;
    
    // Injected fields -- real implementation
    // In order for @Spy / @InjectMocks to work, it's important that these are *classes*, not interfaces.

    @Spy
    @InjectMocks
    private ClinicalDataServiceImpl clinicalDataService;

    @Spy
    @InjectMocks
    private ClinicalAttributeServiceImpl clinicalAttributeService;

    // no dependencies
    @Spy
    private StudyViewFilterUtil studyViewFilterUtil;

    @Spy
    @InjectMocks
    private StudyViewFilterApplier studyViewFilterApplier;
    
    @Spy
    @InjectMocks
    private SampleServiceImpl sampleService;

    @Spy
    @InjectMocks
    private ClinicalDataBinUtil clinicalDataBinUtil;
    
    @Spy
    @InjectMocks
    private ClinicalDataFetcher clinicalDataFetcher;
    
    // Injected fields -- we will mock these out

    @Mock
    private ClinicalDataRepository clinicalDataRepository;

    @Mock
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @Mock
    private SampleRepository sampleRepository;
    
    @Before
    public void setup() {
        // This is necessary for initializing Mockito mocks pre-JUnit 4.5
        // See: https://stackoverflow.com/a/15494996/4077294
        MockitoAnnotations.initMocks(this);

//        List<Sample> samples = mockSamples();
//
//        // Methods used by fetchClinicalDataCounts
//
//        // When cross-checking list of attributes requested
//        when(
//            clinicalAttributeRepository.getClinicalAttributesByStudyIdsAndAttributeIds(
//                /* studyIds */ any(),
//                /* attributeIds */ any()
//            )
//        ).thenReturn(mockClinicalAttributes());

//        // When applying the study view filter to get a list of samples
//        when(
//            sampleRepository.fetchSamples(
//                /* studyIds */ any(),
//                /* sampleIds */ any(),
//                /* projection */ any()
//            )
//        ).thenReturn(samples);
//
//        // When tallying the counts
//        when(
//            clinicalDataRepository.fetchClinicalDataCounts(
//                /* studyIds */ any(),
//                /* sampleIds */ any(),
//                /* attributeIds */ any(),
//                /* clinicalDataType */ any(),
//                /* projection */ any()
//            )
//        ).thenReturn(...);
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
//        ).thenReturn(...);
    }

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
    
    @Test
    public void fetchClinicalAttributes() {
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        when(
            clinicalAttributeRepository.fetchClinicalAttributes(
                /* studyIds */ any(),
                /* projection */ any()
            )
        ).thenReturn(attribs);
        
        var result = this.federatedDataSourceService.fetchClinicalAttributes();

        assertEquals(attribs, result);
    }
}

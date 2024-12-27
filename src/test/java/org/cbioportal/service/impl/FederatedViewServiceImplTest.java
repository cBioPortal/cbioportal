package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.exception.FederationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FederatedViewServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private FederatedViewServiceImpl federatedViewService;

    @Mock
    private List<String> dataSourceStudies = List.of(
        STUDY_ID,
        STUDY_ID_2
    );
    
    @Mock
    private ClinicalAttributeService clinicalAttributeService;
    
    @Mock
    private ClinicalDataService clinicalDataService;
    
    
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
        ReflectionTestUtils.setField(federatedViewService, "federationMode", FederationMode.DATASOURCE);
        
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        
        // Methods used by fetchClinicalAttributes
    }
    
    @Test
    public void fetchClinicalAttributes() throws FederationException {
        // Arrange
        var expected = mockClinicalAttributes();
        
        // Act
        var result = federatedViewService.fetchClinicalAttributes();
        
        // Assert
        Assert.assertEquals(expected, result);
    }
    
//    @Test
//    public void fetchClinicalDataCountsNoFilter() throws FederationException {
//        // Arrange
//        // Mock out: the cohort, ie.
//        // - 1 person is this race & this age & this study
//        // - .. 2 other people ..
//        var filter = new ClinicalDataCountFilter();
//        // First we get the cohort
////        Mockito.when(
////            studyViewFilterApplier.apply()
////        ).thenReturn(
////            
////        );
//        // Then we extract the study and sample IDs from this cohort
//        Mockito.when().thenReturn();
//        // Then we call clinicalDataService
//        Mockito.when(
//            clinicalDataService.fetchClinicalDataCounts(
//                studyIds,
//                sampleIds,
//                attributeIds
//            )
//        ).thenReturn(
//            /* ... */
//        );
//        
//        // Act
//        var result = federatedViewService.fetchClinicalDataCounts(filter);
//        
//        // Assert
//        // - Returns some list of SampleIdentifiers
//        // - Extracts them into study ID + sample ID list
//        // - Calls fetchClinicalDataCounts()
//    }
//    
//    // TODO test with filter
//    
//    @Test
//    public void fetchClinicalDataBinCountsNoFilter() throws FederationException {
//        // Arrange
//        var filter = new ClinicalDataBinCountFilter();
//        Mockito.when(
//            clinicalDataBinUtil.fetchClinicalDataBinCounts(
//                DataBinMethod.STATIC,
//                filter,
//                false
//            )
//        ).thenReturn(
//            /* ... */
//        );
//        
//        // Act
//        var result = federatedViewService.fetchClinicalDataBinCounts();
//        
//        // Assert ...
//    }
//    
//    // TODO test with filter
}

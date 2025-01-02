package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FederatedDataSourceServiceTest extends BaseServiceImplTest {
    
    // NOTE: see ClinicalDataBinUtilTest for another example of a class where
    // we are mocking out deeply nested dependencies (rather than direct subfields)
    // of the class under test.
    
    private final List<String> ALL_STUDIES = List.of(STUDY_ID, STUDY_ID_2, STUDY_ID_3);
    private final List<String> VISIBLE_STUDIES = List.of(STUDY_ID, STUDY_ID_2);
    
    @Spy
    @InjectMocks
    private FederatedDataSourceService federatedDataSourceService;
    
    // Injected fields -- real implementation
    // In order for @Spy / @InjectMocks to work, it's important that these are *classes*, not interfaces.
    
    @Spy
    private List<String> visibleStudies = VISIBLE_STUDIES;

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
    
    private List<ClinicalAttribute> mockPatientAttributes() {

        var attrib1 = new ClinicalAttribute();
        attrib1.setDisplayName("Sex");
        attrib1.setDescription("Sex");
        attrib1.setDatatype("STRING");
        attrib1.setPatientAttribute(true);
        attrib1.setPriority("1");
        attrib1.setAttrId("SEX");
        attrib1.setCancerStudyIdentifier(STUDY_ID);

        var attrib2 = new ClinicalAttribute();
        attrib2.setDisplayName("Age");
        attrib2.setDescription("Age");
        attrib2.setDatatype("NUMBER");
        attrib2.setPatientAttribute(true);
        attrib2.setPriority("1");
        attrib2.setAttrId("AGE");
        attrib2.setCancerStudyIdentifier(STUDY_ID);

        var attrib3 = new ClinicalAttribute();
        attrib3.setDisplayName("Sex");
        attrib3.setDescription("Sex");
        attrib3.setDatatype("STRING");
        attrib3.setPatientAttribute(true);
        attrib3.setPriority("1");
        attrib3.setAttrId("SEX");
        attrib3.setCancerStudyIdentifier(STUDY_ID_2);

        return List.of(attrib1, attrib2, attrib3);
    }
    
    private List<ClinicalAttribute> mockSampleAttributes() {

        var attrib1 = new ClinicalAttribute();
        attrib1.setDisplayName("Sample Type");
        attrib1.setDescription("Sample Type");
        attrib1.setDatatype("STRING");
        attrib1.setPatientAttribute(false);
        attrib1.setPriority("1");
        attrib1.setAttrId("SAMPLE_TYPE");
        attrib1.setCancerStudyIdentifier(STUDY_ID);

        var attrib2 = new ClinicalAttribute();
        attrib2.setDisplayName("Sample Type");
        attrib2.setDescription("Sample Type");
        attrib2.setDatatype("STRING");
        attrib2.setPatientAttribute(false);
        attrib2.setPriority("1");
        attrib2.setAttrId("SAMPLE_TYPE");
        attrib2.setCancerStudyIdentifier(STUDY_ID_2);
        
        return List.of(attrib1, attrib2);
    }
    
    private List<ClinicalAttribute> mockClinicalAttributes() {
        var result = new ArrayList<ClinicalAttribute>();
        result.addAll(mockPatientAttributes());
        result.addAll(mockSampleAttributes());
        return result;
    }
    
    private List<String> mockUniqueCategories(ClinicalAttribute attrib) {
        return List.of("Male", "Female");
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
    
    private List<ClinicalDataCount> mockClinicalDataCounts(
        InvocationOnMock invocation
    ) {
        List<String> studyIds = invocation.getArgument(0);
        List<String> sampleIds = invocation.getArgument(1);
        List<String> attributeIds = invocation.getArgument(2);
        String clinicalDataType = invocation.getArgument(3);
        String projection = invocation.getArgument(4);
        
        var rnd = new Random(0);
        var result = new ArrayList<ClinicalDataCount>();
        
        if (clinicalDataType.equals("SAMPLE") && projection.equals("SUMMARY")) {
            var sampleAttribs = mockSampleAttributes();
            int upperBound = sampleIds.size();
            
            for (var attrib : sampleAttribs) {
                for (var category : mockUniqueCategories(attrib)) {
                    var cds = new ClinicalDataCount();
                    cds.setAttributeId(attrib.getAttrId());
                    cds.setValue(category);
                    cds.setCount(rnd.nextInt(upperBound+1));
                    result.add(cds);
                }
            }
        } else if (clinicalDataType.equals("PATIENT") && projection.equals("SUMMARY")) {
            var patientAttribs = mockPatientAttributes();
            int upperBound = sampleIds.size();

            for (var attrib : patientAttribs) {
                for (var category : mockUniqueCategories(attrib)) {
                    var cds = new ClinicalDataCount();
                    cds.setAttributeId(attrib.getAttrId());
                    cds.setValue(category);
                    cds.setCount(rnd.nextInt(upperBound+1));
                    result.add(cds);
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported clinical datatype + projection");
        }
        
        return result;
    }
    
    private ClinicalDataCountFilter mockClinicalDataCountFilter() {
        var svf = new StudyViewFilter();
        // Even if the user requests to see all of the studies, we should only return a
        // response for the studies that are actually visible
        svf.setStudyIds(ALL_STUDIES);
        
        var requestedAttributes = new ArrayList<ClinicalDataFilter>();
        for (var attrib : mockClinicalAttributes()) {
            var cdf = new ClinicalDataFilter();
            cdf.setAttributeId(attrib.getAttrId());
            requestedAttributes.add(cdf);
        }
        
        var filt = new ClinicalDataCountFilter();
        filt.setStudyViewFilter(svf);
        filt.setAttributes(requestedAttributes);
        return filt;
    }
    
    @Test
    public void fetchClinicalAttributes() {
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        when(
            clinicalAttributeRepository.fetchClinicalAttributes(
                /* studyIds */ eq(visibleStudies),
                /* projection */ eq("SUMMARY")
            )
        ).thenReturn(attribs);
        
        var result = federatedDataSourceService.fetchClinicalAttributes();

        // Verify results
        assertEquals(attribs, result);
    }
    
    @Test
    public void fetchClinicalDataCounts() {
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        List<String> attribIds = attribs.stream().map(ClinicalAttribute::getAttrId).collect(Collectors.toList());
        List<Sample> samples = mockSamples();
        List<String> sampleIds = samples.stream().map(Sample::getStableId).collect(Collectors.toList());
        ClinicalDataCountFilter filter = mockClinicalDataCountFilter();

        // When cross-checking list of attributes requested
        when(
            clinicalAttributeRepository.getClinicalAttributesByStudyIdsAndAttributeIds(
                /* studyIds */ eq(visibleStudies),
                /* attributeIds */ eq(attribIds)
            )
        ).thenReturn(attribs);

        // When applying the study view filter to get a list of samples
        when(
            sampleRepository.fetchSamples(
                /* studyIds */ eq(visibleStudies),
                /* sampleIds */ eq(sampleIds),
                /* projection */ eq("ID")
            )
        ).thenReturn(samples);

        // When tallying the counts
        when(
            clinicalDataRepository.fetchClinicalDataCounts(
                /* studyIds */ eq(visibleStudies),
                /* sampleIds */ eq(sampleIds),
                /* attributeIds */ eq(attribIds),
                /* clinicalDataType */ any(), // "PATIENT" | "SAMPLE"
                /* projection */ any() // "SUMMARY" | "DETAILED"
            )
        ).thenAnswer(this::mockClinicalDataCounts);

        var result = federatedDataSourceService.fetchClinicalDataCounts(filter);
        
        System.out.println(result);

//        // Verify results
//        // TODO
//        // Verify behavior
//        // Even if the database contains other studies, we should only show the visible studies
//        verify(studyViewFilterApplier).apply(
//            argThat(filt -> filt.getStudyIds().equals(visibleStudies))
//        );
//        verify(clinicalDataService).fetchClinicalDataCounts(
//            List.of(STUDY_ID, STUDY_ID_2),
//            List.of(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
//            List.of("SEX", "AGE")
//        );
    }
}

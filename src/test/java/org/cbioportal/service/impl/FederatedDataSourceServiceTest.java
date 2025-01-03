package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.*;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
    
    @Spy // no dependencies
    private ClinicalAttributeUtil clinicalAttributeUtil;
    
    @Spy
    @InjectMocks
    private PatientServiceImpl patientService;
    
    @Spy
    @InjectMocks
    private IdPopulator idPopulator;
    
    @Spy
    @InjectMocks
    private DataBinner dataBinner;
    
    @Spy
    @InjectMocks
    private DataBinHelper dataBinHelper;
    
    @Spy
    @InjectMocks
    private DiscreteDataBinner discreteDataBinner;
    
    // Injected fields -- we will mock these out

    @Mock
    private ClinicalDataRepository clinicalDataRepository;

    @Mock
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @Mock
    private SampleRepository sampleRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Before
    public void setup() {
        // This is necessary for initializing Mockito mocks pre-JUnit 4.5
        // See: https://stackoverflow.com/a/15494996/4077294
        MockitoAnnotations.initMocks(this);
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

    private List<ClinicalAttribute> mockCategoricalAttributes() {
        return mockClinicalAttributes()
            .stream()
            .filter(this::isCategoricalAttr)
            .collect(Collectors.toList());
    }

    private List<ClinicalAttribute> mockNumericalAttributes() {
        return mockClinicalAttributes()
            .stream()
            .filter(this::isNumericalAttr)
            .collect(Collectors.toList());
    }
    
    private List<String> mockUniqueCategories(ClinicalAttribute attrib) {
        return List.of("Male", "Female");
    }
    
    private List<Patient> mockPatients() {
        var pat1 = new Patient();
        pat1.setStableId(PATIENT_ID_1);
        pat1.setCancerStudyIdentifier(STUDY_ID);
        
        var pat2 = new Patient();
        pat2.setStableId(PATIENT_ID_2);
        pat2.setCancerStudyIdentifier(STUDY_ID);
        
        var pat3 = new Patient();
        pat3.setStableId(PATIENT_ID_3);
        pat3.setCancerStudyIdentifier(STUDY_ID_2);
        
        return List.of(pat1, pat2, pat3);
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

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
    private boolean isCategoricalAttr(ClinicalAttribute attr) {
        return attr.getDatatype().equals("STRING");
    }
    
    private boolean isNumericalAttr(ClinicalAttribute attr) {
        return attr.getDatatype().equals("NUMBER");
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
            var sampleAttribs = mockSampleAttributes()
                .stream()
                .filter(distinctByKey(ClinicalAttribute::getAttrId))
                .filter(this::isCategoricalAttr)
                .collect(Collectors.toList());
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
            var patientAttribs = mockPatientAttributes()
                .stream()
                .filter(distinctByKey(ClinicalAttribute::getAttrId))
                .filter(this::isCategoricalAttr)
                .collect(Collectors.toList());
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
        // The studyViewFilter.studyIds field is not used / ignored for the Federated API
        
        var requestedAttributes = new ArrayList<ClinicalDataFilter>();
        for (var attrib : mockCategoricalAttributes()) {
            var cdf = new ClinicalDataFilter();
            cdf.setAttributeId(attrib.getAttrId());
            requestedAttributes.add(cdf);
        }
        
        var filt = new ClinicalDataCountFilter();
        filt.setStudyViewFilter(svf);
        filt.setAttributes(requestedAttributes);
        return filt;
    }
    
    private ClinicalDataBinCountFilter mockClinicalDataBinCountFilter() {
        var svf = new StudyViewFilter();
        // The studyViewFilter.studyIds field is not used / ignored for the Federated API

        var requestedAttributes = new ArrayList<ClinicalDataBinFilter>();
        for (var attrib : mockNumericalAttributes()) {
            var cdbf = new ClinicalDataBinFilter();
            cdbf.setAttributeId(attrib.getAttrId());
            requestedAttributes.add(cdbf);
        }

        var filt = new ClinicalDataBinCountFilter();
        filt.setStudyViewFilter(svf);
        filt.setAttributes(requestedAttributes);
        return filt;
    }
    
    private List<ClinicalData> mockClinicalData(InvocationOnMock invocation) {
        List<String> studyIds = invocation.getArgument(0);
        List<String> sampleIds = invocation.getArgument(1);
        List<String> attributeIds = invocation.getArgument(2);
        String clinicalDataType = invocation.getArgument(3);
        String projection = invocation.getArgument(4);
        
        var rnd = new Random(42);
        var result = new ArrayList<ClinicalData>();

        if (clinicalDataType.equals("SAMPLE") && projection.equals("SUMMARY")) {
            var sampleAttribs = mockSampleAttributes()
                .stream()
                .filter(distinctByKey(ClinicalAttribute::getAttrId))
                .filter(this::isNumericalAttr)
                .collect(Collectors.toList());

            for (var attrib : sampleAttribs) {
                // Generate a ClinicalData for each sample for this attrib
                for (var sample : mockSamples()) {
                    var cd = new ClinicalData();
                    cd.setAttrId(attrib.getAttrId());
                    cd.setAttrValue(Integer.toString(rnd.nextInt(100)));
                    cd.setClinicalAttribute(attrib);
                    cd.setSampleId(sample.getStableId());
                    cd.setPatientId(sample.getPatientStableId());
                    result.add(cd);
                }
            }
        } else if (clinicalDataType.equals("PATIENT") && projection.equals("SUMMARY")) {
            var patientAttribs = mockPatientAttributes()
                .stream()
                .filter(distinctByKey(ClinicalAttribute::getAttrId))
                .filter(this::isNumericalAttr)
                .collect(Collectors.toList());

            for (var attrib : patientAttribs) {
                // Generate a ClinicalData for each patient for this attrib
                for (var patient : mockPatients()) {
                    var cd = new ClinicalData();
                    cd.setAttrId(attrib.getAttrId());
                    cd.setAttrValue(Integer.toString(rnd.nextInt(100)));
                    cd.setClinicalAttribute(attrib);
                    cd.setPatientId(patient.getStableId());
                    result.add(cd);
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported clinical datatype + projection");
        }

        return result;
    }
    
    @Test
    public void fetchClinicalAttributes() {
        List<ClinicalAttribute> attribs = mockClinicalAttributes();
        when(
            clinicalAttributeRepository.fetchClinicalAttributes(
                /* studyIds */ eq(VISIBLE_STUDIES),
                /* projection */ eq("SUMMARY")
            )
        ).thenReturn(new ArrayList<>(attribs)); // create a copy so the original does not get modified
        
        var result = federatedDataSourceService.fetchClinicalAttributes();

        // Verify results
        assertEquals(attribs, result);
        
        // Verify behavior
        verify(clinicalAttributeService).fetchClinicalAttributes(eq(VISIBLE_STUDIES), eq("SUMMARY"));
        verify(clinicalAttributeRepository).fetchClinicalAttributes(eq(VISIBLE_STUDIES), eq("SUMMARY"));
    }
    
    @Test
    public void fetchClinicalDataCounts() {
        List<ClinicalAttribute> attribs = mockCategoricalAttributes();
        List<Sample> samples = mockSamples();
        List<Patient> patients = mockPatients();
        ClinicalDataCountFilter filter = mockClinicalDataCountFilter();

        // When cross-checking list of attributes requested
        when(
            clinicalAttributeRepository.getClinicalAttributesByStudyIdsAndAttributeIds(
                /* studyIds */ any(),
                /* attributeIds */ any()
            )
        ).thenReturn(new ArrayList<>(attribs));

        // When applying the study view filter to get a list of samples
        when(
            sampleRepository.getAllSamplesInStudies(
                /* studyIds */ any(),
                /* projection */ eq("ID"),
                /* pageSize */ isNull(),
                /* pageNumber */ isNull(),
                /* sortBy */ isNull(),
                /* direction */ isNull()
            )
        ).thenReturn(new ArrayList<>(samples));

        // When tallying the counts
        when(
            clinicalDataRepository.fetchClinicalDataCounts(
                /* studyIds */ any(), // visibleStudies but duplicates allowed
                /* sampleIds */ any(),
                /* attributeIds */ any(),
                /* clinicalDataType */ any(), // "PATIENT" | "SAMPLE"
                /* projection */ any() // "SUMMARY" | "DETAILED"
            )
        ).thenAnswer(this::mockClinicalDataCounts);
        
        // When tallying the counts
        when(
            patientRepository.getPatientsOfSamples(
                /* studyIds */ any(),
                /* sampleIds */ any()
            )
        ).thenReturn(new ArrayList<>(patients));

        var result = federatedDataSourceService.fetchClinicalDataCounts(filter);
        
        // Verify results
        // The mock data contains 2 categorical attributes, SAMPLE_TYPE (sample-level) and SEX (patient-level)
        assertEquals(2, result.size());
        assertEquals("SAMPLE_TYPE", result.get(0).getAttributeId());
        assertEquals(2, result.get(0).getCounts().size());
        assertEquals("SEX", result.get(1).getAttributeId());
        assertEquals(2, result.get(1).getCounts().size());
        
        // Verify behavior
        // Even if the database contains other studies, we should only show the visible studies
        verify(studyViewFilterApplier).apply(
            argThat(filt -> VISIBLE_STUDIES.equals(filt.getStudyIds()))
        );
    }
    
    // TODO this test could be written better if we had better mock data to go off of?
    @Test
    public void fetchClinicalDataBinCounts() {
        List<ClinicalAttribute> attribs = mockNumericalAttributes();
        List<Sample> samples = mockSamples();
        List<Patient> patients = mockPatients();
        ClinicalDataBinCountFilter filter = mockClinicalDataBinCountFilter();

        // When cross-checking list of attributes requested
        when(
            clinicalAttributeRepository.getClinicalAttributesByStudyIdsAndAttributeIds(
                /* studyIds */ any(),
                /* attributeIds */ any()
            )
        ).thenReturn(new ArrayList<>(attribs));

        // When applying the study view filter to get a list of samples
        when(
            sampleRepository.getAllSamplesInStudies(
                /* studyIds */ any(),
                /* projection */ eq("ID"),
                /* pageSize */ isNull(),
                /* pageNumber */ isNull(),
                /* sortBy */ isNull(),
                /* direction */ isNull()
            )
        ).thenReturn(new ArrayList<>(samples));

        // When fetching clinical data for the samples pre-binning
        when(
            clinicalDataRepository.fetchClinicalData(
                /* studyIds */ any(),
                /* sampleIds */ any(),
                /* attributeIds */ any(),
                /* clinicalDataType */ any(),
                /* projection */ any()
            )
        ).thenAnswer(this::mockClinicalData);

        // When getting patient data pre-binning
        when(
            patientRepository.getPatientsOfSamples(
                /* studyIds */ any(),
                /* sampleIds */ any()
            )
        ).thenReturn(new ArrayList<>(patients));
        
        var result = federatedDataSourceService.fetchClinicalDataBinCounts(filter);
        
        // Verify results
        assertEquals(4, result.size()); // TODO
        assertEquals("AGE", result.get(0).getAttributeId());

        // Verify behavior
        // Even if the database contains other studies, we should only show the visible studies
        verify(studyViewFilterApplier).apply(
            argThat(filt -> VISIBLE_STUDIES.equals(filt.getStudyIds()))
        );
    }
}

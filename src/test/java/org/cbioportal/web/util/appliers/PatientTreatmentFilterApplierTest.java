package org.cbioportal.web.util.appliers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.OredPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.PatientTreatmentFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PatientTreatmentFilterApplierTest {
    @Mock
    TreatmentService treatmentService;

    @Spy
    TreatmentRowExtractor treatmentRowExtractor;
    
    @InjectMocks
    PatientTreatmentFilterApplier subject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void filterEmptyList() {
        List<SampleIdentifier> samples = new ArrayList<>();
        StudyViewFilter andedFilters = createAndedFilters(
            Arrays.asList("Fakeazil", "Madeupanib"),
            Arrays.asList("Fabricada", "Fakeamab")
        );
        Mockito
            .when(treatmentService.getAllPatientTreatmentRows(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
            .thenReturn(new ArrayList<>());

        List<SampleIdentifier> actual = subject.filter(samples, andedFilters);
        List<SampleIdentifier> expected = new ArrayList<>();
        
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void filterAllFromList() {
        List<SampleIdentifier> samples = Arrays.asList(
            createSampleId("SA_0", "ST_0"),
            createSampleId("SA_1", "ST_0"),
            createSampleId("SA_2", "ST_1"),
            createSampleId("SA_3", "ST_1")
        );
        StudyViewFilter andedFilters = createAndedFilters(
            Arrays.asList("Improvizox", "Madeupanib"),
            Arrays.asList("Fabricada", "Fakeamab")
        );
        Mockito
            .when(treatmentService.getAllPatientTreatmentRows(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
            .thenReturn(new ArrayList<>());

        List<SampleIdentifier> actual = subject.filter(samples, andedFilters);
        List<SampleIdentifier> expected = new ArrayList<>();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void filterNoneFromList() {
        List<SampleIdentifier> samples = Arrays.asList(
            createSampleId("SA_0", "ST_0"),
            createSampleId("SA_1", "ST_0"),
            createSampleId("SA_2", "ST_1"),
            createSampleId("SA_3", "ST_1")
        );
        StudyViewFilter andedFilters = createAndedFilters(
            // so each sample needs to be from a patient that has recieved...
            Arrays.asList("Improvizox", "Madeupanib"), // one of these treatments
            Arrays.asList("Fabricada", "Fakeamab") // AND one of these treatments
        );
        Mockito
            .when(treatmentService.getAllPatientTreatmentRows(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
            .thenReturn(Arrays.asList(
                new PatientTreatmentRow("Improvizox", 2, toSet(createEvent("SA_0", "ST_0"), createEvent("SA_1", "ST_0"))),
                new PatientTreatmentRow("Fakeamab", 2, toSet(createEvent("SA_0", "ST_0"), createEvent("SA_1", "ST_0"))),
                new PatientTreatmentRow("Madeupanib", 2, toSet(createEvent("SA_2", "ST_1"), createEvent("SA_3", "ST_1"))),
                new PatientTreatmentRow("Fabricada", 2, toSet(createEvent("SA_2", "ST_1"), createEvent("SA_3", "ST_1")))
            ));

        List<SampleIdentifier> actual = subject.filter(samples, andedFilters);
        List<SampleIdentifier> expected = Arrays.asList(
            createSampleId("SA_0", "ST_0"),
            createSampleId("SA_1", "ST_0"),
            createSampleId("SA_2", "ST_1"),
            createSampleId("SA_3", "ST_1")
        );

        Assert.assertEquals(expected, actual);
    }

    private ClinicalEventSample createEvent(String sampleId, String studyId) {
        ClinicalEventSample sample = new ClinicalEventSample();
        sample.setSampleId(sampleId);
        sample.setStudyId(studyId);
        return sample;
    }
    
    private SampleIdentifier createSampleId(String sampleId, String studyId) {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(sampleId);
        sampleIdentifier.setStudyId(studyId);
        return sampleIdentifier;
    }
    
    @SafeVarargs
    private final StudyViewFilter createAndedFilters(List<String>... treatments) {
        AndedPatientTreatmentFilters andedFilters = new AndedPatientTreatmentFilters();
        List<OredPatientTreatmentFilters> oredFilters = Arrays.stream(treatments)
            .map(this::createOredFilters)
            .collect(Collectors.toList());
        andedFilters.setFilters(oredFilters);

        StudyViewFilter filter = new StudyViewFilter();
        filter.setPatientTreatmentFilters(andedFilters);
        return filter;
    }
    
    private OredPatientTreatmentFilters createOredFilters(List<String> treatments) {
        OredPatientTreatmentFilters oredFilters = new OredPatientTreatmentFilters();
        List<PatientTreatmentFilter> filters = treatments.stream()
            .map(this::createFilter)
            .collect(Collectors.toList());
        oredFilters.setFilters(filters);
        return oredFilters;
    }
    
    private PatientTreatmentFilter createFilter(String treatment) {
        PatientTreatmentFilter filter = new PatientTreatmentFilter();
        filter.setTreatment(treatment);
        return filter;
    }
    
    private Set<ClinicalEventSample> toSet(ClinicalEventSample... samples) {
        return new HashSet<>(Arrays.asList(samples));
    }
}
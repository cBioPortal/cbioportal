package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.cbioportal.web.parameter.filter.OredSampleTreatmentFilters;
import org.cbioportal.web.parameter.filter.SampleTreatmentFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cbioportal.model.TemporalRelation.Post;
import static org.cbioportal.model.TemporalRelation.Pre;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SampleTreatmentFilterApplierTest {
    @Mock
    TreatmentService treatmentService;

    @InjectMocks
    SampleTreatmentFilterApplier subject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void filterEmptyList() {
        List<SampleIdentifier> samples = new ArrayList<>();
        AndedSampleTreatmentFilters andedFilters = createAndedFilters(
            Arrays.asList(new Pair<>("Fakeazil", Pre), new Pair<>("Madeupanib", Post)),
            Arrays.asList(new Pair<>("Fabricada", Pre), new Pair<>("Fakeamab", Post))
        );
        Mockito
            .when(treatmentService.getAllSampleTreatmentRows(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(new ArrayList<>());

        List<SampleIdentifier> actual = subject.filter(andedFilters, samples);
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
        AndedSampleTreatmentFilters andedFilters = createAndedFilters(
            Arrays.asList(new Pair<>("Improvizox", Pre), new Pair<>("Madeupanib", Post)),
            Arrays.asList(new Pair<>("Fabricada", Pre), new Pair<>("Fakeamab", Post))
        );
        Mockito
            .when(treatmentService.getAllSampleTreatmentRows(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(new ArrayList<>());

        List<SampleIdentifier> actual = subject.filter(andedFilters, samples);
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
        AndedSampleTreatmentFilters andedFilters = createAndedFilters(
            // so each sample needs to be...
            // before Improvizox or after Madeupanib
            Arrays.asList(new Pair<>("Improvizox", Pre), new Pair<>("Madeupanib", Post)),
            // AND before Fabricada or after Fakeamab
            Arrays.asList(new Pair<>("Fabricada", Pre), new Pair<>("Fakeamab", Post))
        );
        Mockito
            .when(treatmentService.getAllSampleTreatmentRows(Mockito.anyList(), Mockito.anyList()))
            .thenReturn(Arrays.asList(
                new SampleTreatmentRow(Pre, "Improvizox", 2, toSet(createEvent("SA_0", "ST_0"), createEvent("SA_1", "ST_0"))),
                new SampleTreatmentRow(Post, "Fakeamab", 2,  toSet(createEvent("SA_0", "ST_0"), createEvent("SA_1", "ST_0"))),
                new SampleTreatmentRow(Post, "Madeupanib", 2,  toSet(createEvent("SA_2", "ST_1"), createEvent("SA_3", "ST_1"))),
                new SampleTreatmentRow(Pre, "Fabricada", 2, toSet(createEvent("SA_2", "ST_1"), createEvent("SA_3", "ST_1")))
            ));

        List<SampleIdentifier> actual = subject.filter(andedFilters, samples);
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
    private final AndedSampleTreatmentFilters createAndedFilters(List<Pair<String, TemporalRelation>>... treatments) {
        AndedSampleTreatmentFilters andedFilters = new AndedSampleTreatmentFilters();
        List<OredSampleTreatmentFilters> oredFilters = Arrays.stream(treatments)
            .map(this::createOredFilters)
            .collect(Collectors.toList());
        andedFilters.setFilters(oredFilters);

        return andedFilters;
    }

    private OredSampleTreatmentFilters createOredFilters(List<Pair<String, TemporalRelation>> treatments) {
        OredSampleTreatmentFilters oredFilters = new OredSampleTreatmentFilters();
        List<SampleTreatmentFilter> filters = treatments.stream()
            .map(this::createFilter)
            .collect(Collectors.toList());
        oredFilters.setFilters(filters);
        return oredFilters;
    }

    private SampleTreatmentFilter createFilter(Pair<String, TemporalRelation> pair) {
        SampleTreatmentFilter filter = new SampleTreatmentFilter();
        filter.setTreatment(pair.a);
        filter.setTime(pair.b);
        return filter;
    }

    private Set<ClinicalEventSample> toSet(ClinicalEventSample... samples) {
        return new HashSet<>(Arrays.asList(samples));
    }
    
    private static final class Pair<A, B> {
        final A a;
        final B b;

        Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
}
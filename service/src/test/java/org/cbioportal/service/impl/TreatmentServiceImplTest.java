package org.cbioportal.service.impl;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.model.Treatment;
import org.cbioportal.model.TreatmentRow;
import org.cbioportal.persistence.TreatmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TreatmentServiceImplTest {
    @InjectMocks
    private TreatmentServiceImpl treatmentService;
    
    @Mock
    private TreatmentRepository treatmentRepository;
    
    private final List<String> sampleIds = Collections.singletonList("1");
    private final List<String> studyIds = Collections.singletonList("Foo");

    @Test
    public void sampleBeforeTreatment() {
        Treatment tA = createTreatment("A", 0, 10, 20);
        DatedSample sample = createSample(0, 5);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(tA));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(sample));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
            new TreatmentRow(TemporalRelation.Pre, "A", 1)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void sampleAfterTreatment() {
        Treatment tA = createTreatment("A", 0, 10, 20);
        DatedSample sample = createSample(0, 30);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(tA));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(sample));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
            new TreatmentRow(TemporalRelation.Post, "A", 1)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void sampleDuringTreatment() {
        Treatment tA = createTreatment("A", 0, 10, 20);
        DatedSample sample = createSample(0, 15);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(tA));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(sample));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
            new TreatmentRow(TemporalRelation.Post, "A", 1)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void sampleNoDate() {
        Treatment tA = createTreatment("A", 0, 10, 20);
        DatedSample sample = createSample(0, null);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(tA));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(sample));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
            new TreatmentRow(TemporalRelation.Unknown, "A", 1)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void sampleWrappedBySameTreatmentShouldOnlyBePost() {
        Treatment tA = createTreatment("A", 0, 0, 9);
        Treatment tB = createTreatment("A", 0, 20, 29);
        DatedSample sample = createSample(0, 15);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Arrays.asList(tA, tB));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Collections.singletonList(sample));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
                new TreatmentRow(TemporalRelation.Post, "A", 1)
        );
        
        assertRowsEqual(expected, actual);
    }

    @Test
    public void singleTreatmentTypeSinglePatientNoOverlaps() {
        Treatment treatmentA = createTreatment("A", 0, 5, 9);
        Treatment treatmentB = createTreatment("A", 0, 20, 29);
        Treatment treatmentC = createTreatment("A", 0, 40, 49);
        DatedSample sampleC  = createSample(0, 0);
        DatedSample sampleA = createSample(0, 15);
        DatedSample sampleB  = createSample(0, 35);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Arrays.asList(treatmentA, treatmentB, treatmentC));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Arrays.asList(sampleA, sampleB, sampleC));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Arrays.asList(
            new TreatmentRow(TemporalRelation.Post, "A", 2),
            new TreatmentRow(TemporalRelation.Pre, "A", 1)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void multipleTreatmentTypeSinglePatientNoOverlaps() {
        Treatment treatmentA = createTreatment("A", 0, 5, 9);
        Treatment treatmentB = createTreatment("B", 0, 20, 29);
        Treatment treatmentC = createTreatment("C", 0, 40, 49);
        DatedSample sampleC  = createSample(0, 0);
        DatedSample sampleA = createSample(0, 15);
        DatedSample sampleB  = createSample(0, 35);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Arrays.asList(treatmentA, treatmentB, treatmentC));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Arrays.asList(sampleA, sampleB, sampleC));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Arrays.asList(
            new TreatmentRow(TemporalRelation.Post, "A", 2),
            new TreatmentRow(TemporalRelation.Pre, "A", 1),
            new TreatmentRow(TemporalRelation.Post, "B", 1),
            new TreatmentRow(TemporalRelation.Pre, "B", 2),
            new TreatmentRow(TemporalRelation.Pre, "C", 3)
        );

        assertRowsEqual(expected, actual);
    }

    @Test
    public void singleTreatmentTypeMultiplePatientNoOverlaps() {
        Treatment treatmentA = createTreatment("A", 0, 0, 9);
        Treatment treatmentB = createTreatment("A", 1, 20, 29);
        Treatment treatmentC = createTreatment("A", 2, 40, 49);
        DatedSample sampleA = createSample(0, 15);
        DatedSample sampleB  = createSample(1, 35);
        DatedSample sampleC  = createSample(2, 55);

        Mockito.when(treatmentRepository.getAllTreatments(sampleIds, studyIds))
            .thenReturn(Arrays.asList(treatmentA, treatmentB, treatmentC));
        Mockito.when(treatmentRepository.getAllSamples(sampleIds, studyIds))
            .thenReturn(Arrays.asList(sampleA, sampleB, sampleC));

        List<TreatmentRow> actual = treatmentService.getAllTreatmentRows(sampleIds, studyIds);
        List<TreatmentRow> expected = Collections.singletonList(
            new TreatmentRow(TemporalRelation.Post, "A", 3)
        );

        assertRowsEqual(expected, actual);
    }
    
    private void assertRowsEqual(List<TreatmentRow> expected, List<TreatmentRow> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TreatmentRow expectedRow = expected.get(i);
            TreatmentRow actualRow = actual.get(i);
            assertTrue(new ReflectionEquals(expectedRow).matches(actualRow));
        }
    }
    
    private DatedSample createSample(Integer patientId, Integer timeTaken) {
        DatedSample sample = new DatedSample();
        sample.setPatientId(patientId);
        sample.setTimeTaken(timeTaken);
        return sample;
    }
    
    private Treatment createTreatment(String treatment, Integer patientId, Integer start, Integer stop) {
        Treatment t = new Treatment();
        t.setTreatment(treatment);
        t.setPatientId(patientId);
        t.setStart(start);
        t.setStop(stop);
        return t;
    }
}
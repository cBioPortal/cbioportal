package org.cbioportal.domain.clinical_event.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.clinical_event.TumorVolumeChange;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetTumorVolumeChangeTest {

  @Mock private ClinicalEventRepository clinicalEventRepository;
  @Mock private PatientRepository patientRepository;
  @InjectMocks private GetTumorVolumeChange useCase;

  @Test
  public void calculatesChangeUsingRequestedBaseline() {
    patients("p1");
    when(clinicalEventRepository.getPatientClinicalEvents("study", "p1"))
        .thenReturn(List.of(event(0, "200"), event(10, "100"), event(20, "75")));

    assertEquals(
        List.of(new TumorVolumeChange("study", "p1", -25)),
        useCase.calculateTumorVolumePercentageChange("study", 10, 20));
  }

  @Test
  public void excludesMissingZeroAndNonFiniteMeasurements() {
    patients("missing", "zero", "nonfinite", "valid");
    when(clinicalEventRepository.getPatientClinicalEvents("study", "missing"))
        .thenReturn(List.of(event(0, "100")));
    when(clinicalEventRepository.getPatientClinicalEvents("study", "zero"))
        .thenReturn(List.of(event(0, "0"), event(10, "10")));
    when(clinicalEventRepository.getPatientClinicalEvents("study", "nonfinite"))
        .thenReturn(List.of(event(0, "100"), event(10, "NaN")));
    when(clinicalEventRepository.getPatientClinicalEvents("study", "valid"))
        .thenReturn(List.of(event(0, "100"), event(10, "150")));

    assertEquals(
        List.of(new TumorVolumeChange("study", "valid", 50)),
        useCase.calculateTumorVolumePercentageChange("study", 0, 10));
  }

  @Test
  public void returnsEmptyForStudyWithoutPatients() {
    patients();
    assertTrue(useCase.calculateTumorVolumePercentageChange("study", 0, 10).isEmpty());
  }

  @Test
  public void readsSubsequentPatientPages() {
    Patient first = patient("first");
    when(patientRepository.getAllPatientsInStudy("study", "SUMMARY", 500, 0, "stableId", "ASC"))
        .thenReturn(java.util.Collections.nCopies(500, first));
    when(patientRepository.getAllPatientsInStudy("study", "SUMMARY", 500, 1, "stableId", "ASC"))
        .thenReturn(List.of(patient("last")));
    when(clinicalEventRepository.getPatientClinicalEvents("study", "first")).thenReturn(List.of());
    when(clinicalEventRepository.getPatientClinicalEvents("study", "last"))
        .thenReturn(List.of(event(0, "100"), event(10, "150")));

    assertEquals(
        List.of(new TumorVolumeChange("study", "last", 50)),
        useCase.calculateTumorVolumePercentageChange("study", 0, 10));
  }

  private void patients(String... ids) {
    when(patientRepository.getAllPatientsInStudy("study", "SUMMARY", 500, 0, "stableId", "ASC"))
        .thenReturn(java.util.Arrays.stream(ids).map(GetTumorVolumeChangeTest::patient).toList());
  }

  private static Patient patient(String id) {
    Patient patient = new Patient();
    patient.setStableId(id);
    return patient;
  }

  private static ClinicalEvent event(int day, String value) {
    ClinicalEventData attribute = new ClinicalEventData();
    attribute.setKey("TUMOR_VOLUME");
    attribute.setValue(value);
    ClinicalEvent event = new ClinicalEvent();
    event.setStartDate(day);
    event.setAttributes(List.of(attribute));
    return event;
  }
}

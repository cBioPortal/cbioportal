package org.cbioportal.domain.clinical_event.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.clinical_event.TumorVolumeChange;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Calculates patient tumor-volume percentage changes between two timepoints. */
@Service
public class GetTumorVolumeChange {

  private static final Logger LOG = LoggerFactory.getLogger(GetTumorVolumeChange.class);
  private static final int PATIENT_PAGE_SIZE = 500;
  private final ClinicalEventRepository clinicalEventRepository;
  private final PatientRepository patientRepository;

  /**
   * Constructs a use case for retrieving tumor volume changes of clinical event type TUMOR_VOLUME.
   *
   * @param clinicalEventRepository The repository used to fetch clinical event type counts.
   * @param patientRepository The repository used to fetch patient data.
   */
  public GetTumorVolumeChange(
      ClinicalEventRepository clinicalEventRepository, PatientRepository patientRepository) {
    this.clinicalEventRepository = clinicalEventRepository;
    this.patientRepository = patientRepository;
  }

  /**
   * Patients with missing measurements or a zero baseline are excluded.
   *
   * @return A list of {@link TumorVolumeChange} representing the tumor volume changes of clinical
   *     event type TUMOR_VOLUME.
   */
  public List<TumorVolumeChange> calculateTumorVolumePercentageChange(
      String studyId, Integer baselineTimepoint, Integer measurementTimepoint) {
    List<TumorVolumeChange> results = new ArrayList<>();
    for (int pageNumber = 0; ; pageNumber++) {
      List<Patient> patients =
          patientRepository.getAllPatientsInStudy(
              studyId, "SUMMARY", PATIENT_PAGE_SIZE, pageNumber, "stableId", "ASC");
      for (Patient patient : patients) {
        var events =
            clinicalEventRepository.getPatientClinicalEvents(studyId, patient.getStableId());
        Double percentageChange =
            calculatePatientTumorVolumePercentageChange(
                events, baselineTimepoint, measurementTimepoint);
        if (percentageChange == null) {
          LOG.debug(
              "Excluding patient {} in study {}: missing or non-finite measurement, zero baseline,"
                  + " or non-finite percentage change",
              patient.getStableId(),
              studyId);
          continue;
        }
        results.add(new TumorVolumeChange(studyId, patient.getStableId(), percentageChange));
      }
      if (patients.size() < PATIENT_PAGE_SIZE) {
        break;
      }
    }
    return results;
  }

  private Double calculatePatientTumorVolumePercentageChange(
      List<ClinicalEvent> events, Integer baselineTimepoint, Integer measurementTimepoint) {
    Double baselineValue = getMeasurementValueAtTimepoint(events, baselineTimepoint);
    Double measurementValue = getMeasurementValueAtTimepoint(events, measurementTimepoint);
    if (baselineValue == null
        || measurementValue == null
        || baselineValue == 0
        || !Double.isFinite(baselineValue)
        || !Double.isFinite(measurementValue)) {
      return null;
    }
    double percentageChange = (measurementValue - baselineValue) / baselineValue * 100;
    return Double.isFinite(percentageChange) ? percentageChange : null;
  }

  private Double getMeasurementValueAtTimepoint(List<ClinicalEvent> events, Integer timepoint) {
    for (ClinicalEvent event : events) {
      if (timepoint.equals(event.getStartDate()) && event.getAttributes() != null) {
        for (ClinicalEventData attribute : event.getAttributes()) {
          if ("TUMOR_VOLUME".equals(attribute.getKey())) {
            String value = attribute.getValue();
            return value == null || value.isBlank() ? null : Double.valueOf(value);
          }
        }
      }
    }
    return null;
  }
}

package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiHierarchy;

/** Reads the materialized whole-slide-image hierarchy for a study and patient. */
public interface WsiHierarchyRepository {

  /**
   * Returns the normalized hierarchy for a patient.
   *
   * @return the hierarchy; an empty hierarchy (no sample groups, null reference sample) when the
   *     study and patient exist but the patient has no WSI resource rows; or {@code null} when the
   *     study or patient does not exist, or the rows fail de-identification checks
   */
  WsiHierarchy getPatientHierarchy(String studyId, String patientId);
}

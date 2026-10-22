package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiHierarchy;

/** Reads the materialized whole-slide-image hierarchy for a study and patient. */
public interface WsiHierarchyRepository {

  /**
   * Returns the hierarchy for a patient, built from the patient's WSI resource rows.
   *
   * <p>Slides are addressed only by their opaque slide key. Implementations never expose the slide
   * barcode, resource-data row identifiers or the private wsi_serving object, and omit slides that
   * have no valid slide key.
   *
   * @return the hierarchy; an empty hierarchy (no sample groups, null reference sample) when the
   *     study and patient exist but the patient has no WSI resource rows; or {@code null} when the
   *     study or patient does not exist, or the rows fail de-identification checks
   */
  WsiHierarchy getPatientHierarchy(String studyId, String patientId);
}

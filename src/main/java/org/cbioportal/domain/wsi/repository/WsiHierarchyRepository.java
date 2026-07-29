package org.cbioportal.domain.wsi.repository;

/** Reads the materialized whole-slide-image hierarchy for a study and patient. */
public interface WsiHierarchyRepository {

  /** Returns active hierarchy JSON, or {@code null} when no hierarchy exists. */
  String getPatientHierarchy(String studyId, String patientId);
}

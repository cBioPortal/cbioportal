package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiHierarchy;

/** Reads the materialized whole-slide-image hierarchy for a study and patient. */
public interface WsiHierarchyRepository {

  /** Returns the active normalized hierarchy, or {@code null} when no hierarchy exists. */
  WsiHierarchy getPatientHierarchy(String studyId, String patientId);
}

package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiSlideAccess;

/** Reads the active, fully materialized pixel-access data for one slide. */
public interface WsiSlideAccessRepository {

  /**
   * Returns access data for the patient's servable slide with this image ID, or {@code null} when
   * absent/incomplete.
   */
  WsiSlideAccess getSlideAccess(String studyId, String patientId, String imageId);
}

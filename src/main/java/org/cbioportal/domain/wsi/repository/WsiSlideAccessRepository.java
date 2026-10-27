package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiSlideSource;

/** Looks up the tile-capability inputs of one slide. */
public interface WsiSlideAccessRepository {

  /**
   * Returns the slide of the study's patient addressed by its slide key, or {@code null} when there
   * is no such WSI_SAMPLE/WSI_PATIENT row or the row cannot serve tiles.
   */
  WsiSlideSource getSlideSource(String studyId, String patientId, String slideKey);
}

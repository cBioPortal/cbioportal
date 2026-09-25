package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiSlideAccess;

/** Reads the active, fully materialized pixel-access data for one slide. */
public interface WsiSlideAccessRepository {

  /** Returns access data for a servable slide, or {@code null} when absent/incomplete. */
  WsiSlideAccess getSlideAccess(String studyId, String imageId);
}

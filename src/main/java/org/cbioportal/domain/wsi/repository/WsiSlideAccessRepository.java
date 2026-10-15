package org.cbioportal.domain.wsi.repository;

import org.cbioportal.domain.wsi.WsiSlideSource;

/** Reads the active, fully materialized pixel-access data for one slide. */
public interface WsiSlideAccessRepository {

  /**
   * Returns the server-side pixel source for the patient's servable slide addressed by its opaque
   * slide key, or {@code null} when absent/incomplete. The result is never returned to the browser
   * directly.
   *
   * <p>Implementations own the slide-key binding: a non-null result always has {@code
   * slideKey().equals(slideKey)}, and malformed keys yield {@code null}.
   */
  WsiSlideSource getSlideSource(String studyId, String patientId, String slideKey);
}

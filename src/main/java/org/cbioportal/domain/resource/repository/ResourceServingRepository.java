package org.cbioportal.domain.resource.repository;

import org.cbioportal.domain.resource.ResourceServing;

/** Looks up the serving data of one resource row. */
public interface ResourceServingRepository {

  /**
   * Returns the serving data of the study's {@code resourceId} row whose {@code
   * metadata.serving.key} is {@code key}, or {@code null} when there is none or it is incomplete. A
   * non-null {@code patientId} narrows the lookup to that patient's rows.
   */
  ResourceServing getServing(String studyId, String resourceId, String key, String patientId);
}

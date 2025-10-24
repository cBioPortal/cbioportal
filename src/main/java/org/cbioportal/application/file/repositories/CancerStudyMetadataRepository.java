package org.cbioportal.application.file.repositories;

import java.util.List;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

/** Repository interface for retrieving cancer study metadata and cancer type hierarchy. */
public interface CancerStudyMetadataRepository {
  /**
   * Retrieves metadata for a specific cancer study.
   *
   * @param studyId the identifier of the cancer study
   * @return the metadata of the specified cancer study
   */
  CancerStudyMetadata getCancerStudyMetadata(String studyId);

  /**
   * Retrieves the hierarchy (cancer type + ancestors) of cancer types for a specific study.
   *
   * @param studyId
   * @return a list of CancerType objects representing the hierarchy of cancer types for the
   *     specified study
   */
  List<CancerType> getCancerTypeHierarchy(String studyId);
}

package org.cbioportal.application.file.export.mappers;

import java.util.List;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

/** Mapper interface for retrieving cancer study metadata and cancer type hierarchy. */
public interface CancerStudyMetadataMapper {
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
   * @return
   */
  List<CancerType> getCancerTypeHierarchy(String studyId);
}

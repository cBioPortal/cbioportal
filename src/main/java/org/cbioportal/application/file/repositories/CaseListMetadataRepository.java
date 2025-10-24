package org.cbioportal.application.file.repositories;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.CaseListMetadata;

/** Repository interface for retrieving metadata for case lists. */
public interface CaseListMetadataRepository {
  /**
   * Retrieves metadata for a specific case list.
   *
   * @param studyId the identifier of the study
   * @param sampleIds filter for specific sample IDs within the case list
   * @return a list of CaseListMetadata objects containing metadata for the specified case list
   */
  List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds);
}

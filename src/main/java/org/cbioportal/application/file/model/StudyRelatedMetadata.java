package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public interface StudyRelatedMetadata {
  String getCancerStudyIdentifier();

  /**
   * Get the metadata key-value pairs for this metadata object. Used to write metadata to a file.
   *
   * @return A sequenced map of metadata key-value pairs.
   */
  default SequencedMap<String, String> toMetadataKeyValues() {
    var metadata = new LinkedHashMap<String, String>();
    metadata.put("cancer_study_identifier", getCancerStudyIdentifier());
    return metadata;
  }
}

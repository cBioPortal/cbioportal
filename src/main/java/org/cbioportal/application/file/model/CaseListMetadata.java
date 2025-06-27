package org.cbioportal.application.file.model;

import java.util.SequencedMap;
import java.util.SequencedSet;

/** Represents metadata for a case list. */
public class CaseListMetadata implements StudyRelatedMetadata {
  private String cancerStudyIdentifier;
  private String stableId;
  private String name;
  private String description;
  private SequencedSet<String> sampleIds;

  public CaseListMetadata() {}

  /**
   * Get the stable ID of the case list without the cancer study identifier.
   *
   * @return
   */
  public String getCaseListTypeStableId() {
    if (stableId == null) {
      return null;
    }
    if (cancerStudyIdentifier == null) {
      return stableId;
    }
    return stableId.replace(this.cancerStudyIdentifier + "_", "");
  }

  @Override
  public String getCancerStudyIdentifier() {
    return cancerStudyIdentifier;
  }

  public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
    this.cancerStudyIdentifier = cancerStudyIdentifier;
  }

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SequencedSet<String> getSampleIds() {
    return sampleIds;
  }

  public void setSampleIds(SequencedSet<String> sampleIds) {
    this.sampleIds = sampleIds;
  }

  @Override
  public SequencedMap<String, String> toMetadataKeyValues() {
    var metadata = StudyRelatedMetadata.super.toMetadataKeyValues();
    metadata.put("stable_id", getStableId());
    metadata.put("case_list_name", getName());
    metadata.put("case_list_description", getDescription());
    metadata.put("case_list_ids", String.join("\t", getSampleIds()));
    return metadata;
  }
}

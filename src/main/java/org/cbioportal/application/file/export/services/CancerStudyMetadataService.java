package org.cbioportal.application.file.export.services;

import java.util.List;
import org.cbioportal.application.file.export.repositories.CancerStudyMetadataRepository;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

public class CancerStudyMetadataService {

  private final CancerStudyMetadataRepository cancerStudyMetadataRepository;

  public CancerStudyMetadataService(CancerStudyMetadataRepository cancerStudyMetadataRepository) {
    this.cancerStudyMetadataRepository = cancerStudyMetadataRepository;
  }

  public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
    return cancerStudyMetadataRepository.getCancerStudyMetadata(studyId);
  }

  public List<CancerType> getCancerTypeHierarchy(String studyId) {
    return cancerStudyMetadataRepository.getCancerTypeHierarchy(studyId);
  }
}

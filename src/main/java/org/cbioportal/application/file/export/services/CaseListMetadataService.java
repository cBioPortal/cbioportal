package org.cbioportal.application.file.export.services;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.CaseListMetadataRepository;
import org.cbioportal.application.file.model.CaseListMetadata;

public class CaseListMetadataService {

  private final CaseListMetadataRepository caseListMetadataRepository;

  public CaseListMetadataService(CaseListMetadataRepository caseListMetadataRepository) {
    this.caseListMetadataRepository = caseListMetadataRepository;
  }

  public List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds) {
    return caseListMetadataRepository.getCaseListsMetadata(studyId, sampleIds);
  }
}

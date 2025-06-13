package org.cbioportal.application.file.export.services;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.mappers.CaseListMetadataMapper;
import org.cbioportal.application.file.model.CaseListMetadata;

public class CaseListMetadataService {

  private final CaseListMetadataMapper caseListMetadataMapper;

  public CaseListMetadataService(CaseListMetadataMapper caseListMetadataMapper) {
    this.caseListMetadataMapper = caseListMetadataMapper;
  }

  public List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds) {
    return caseListMetadataMapper.getCaseListsMetadata(studyId, sampleIds);
  }
}

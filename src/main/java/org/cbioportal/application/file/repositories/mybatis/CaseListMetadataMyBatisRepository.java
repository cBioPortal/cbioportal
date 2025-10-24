package org.cbioportal.application.file.repositories.mybatis;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.repositories.CaseListMetadataRepository;
import org.cbioportal.application.file.model.CaseListMetadata;

public class CaseListMetadataMyBatisRepository implements CaseListMetadataRepository {
  private final CaseListMetadataMapper mapper;

  public CaseListMetadataMyBatisRepository(CaseListMetadataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds) {
    return mapper.getCaseListsMetadata(studyId, sampleIds);
  }
}

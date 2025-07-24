package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.List;
import org.cbioportal.application.file.export.repositories.CancerStudyMetadataRepository;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

public class CancerStudyMetadataMyBatisRepository implements CancerStudyMetadataRepository {

  private final CancerStudyMetadataMapper mapper;

  public CancerStudyMetadataMyBatisRepository(CancerStudyMetadataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
    return mapper.getCancerStudyMetadata(studyId);
  }

  @Override
  public List<CancerType> getCancerTypeHierarchy(String studyId) {
    return mapper.getCancerTypeHierarchy(studyId);
  }
}

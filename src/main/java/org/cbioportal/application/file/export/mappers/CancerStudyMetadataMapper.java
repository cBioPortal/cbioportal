package org.cbioportal.application.file.export.mappers;

import java.util.List;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

public interface CancerStudyMetadataMapper {
  CancerStudyMetadata getCancerStudyMetadata(String studyId);

  List<CancerType> getCancerTypeHierarchy(String studyId);
}

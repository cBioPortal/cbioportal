package org.cbioportal.application.file.export.mappers;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.CaseListMetadata;

public interface CaseListMetadataMapper {
  List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds);
}

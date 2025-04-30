package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.CaseListMetadata;

import java.util.List;
import java.util.Set;

public interface CaseListMetadataMapper {
    List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds);
}

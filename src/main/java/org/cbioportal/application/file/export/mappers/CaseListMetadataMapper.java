package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.CaseListMetadata;

import java.util.List;

public interface CaseListMetadataMapper {
    List<CaseListMetadata> getCaseListsMetadata(String studyId);
}

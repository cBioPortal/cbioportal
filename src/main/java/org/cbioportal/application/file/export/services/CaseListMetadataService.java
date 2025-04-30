package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.CaseListMetadataMapper;
import org.cbioportal.application.file.model.CaseListMetadata;

import java.util.List;
import java.util.Set;

public class CaseListMetadataService {

    private final CaseListMetadataMapper caseListMetadataMapper;

    public CaseListMetadataService(CaseListMetadataMapper caseListMetadataMapper) {
        this.caseListMetadataMapper = caseListMetadataMapper;
    }

    public List<CaseListMetadata> getCaseListsMetadata(String studyId, Set<String> sampleIds) {
        return caseListMetadataMapper.getCaseListsMetadata(studyId, sampleIds);
    }
}

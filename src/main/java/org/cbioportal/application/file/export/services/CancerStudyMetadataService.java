package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.CancerStudyMetadataMapper;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

import java.util.List;

public class CancerStudyMetadataService {

    private final CancerStudyMetadataMapper cancerStudyMetadataMapper;

    public CancerStudyMetadataService(CancerStudyMetadataMapper cancerStudyMetadataMapper) {
        this.cancerStudyMetadataMapper = cancerStudyMetadataMapper;
    }

    public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
        return cancerStudyMetadataMapper.getCancerStudyMetadata(studyId);
    }

    public List<CancerType> getCancerTypeHierarchy(String studyId) {
        return cancerStudyMetadataMapper.getCancerTypeHierarchy(studyId);
    }
}

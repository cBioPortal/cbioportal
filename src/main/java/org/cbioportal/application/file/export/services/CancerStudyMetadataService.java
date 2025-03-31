package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.CancerStudyMetadataMapper;
import org.cbioportal.application.file.model.CancerStudyMetadata;

public class CancerStudyMetadataService {

    private final CancerStudyMetadataMapper cancerStudyMetadataMapper;

    public CancerStudyMetadataService(CancerStudyMetadataMapper cancerStudyMetadataMapper) {
        this.cancerStudyMetadataMapper = cancerStudyMetadataMapper;
    }

    public CancerStudyMetadata getCancerStudyMetadata(String studyId) {
        return cancerStudyMetadataMapper.getCancerStudyMetadata(studyId);
    }
}

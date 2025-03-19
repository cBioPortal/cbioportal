package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.CancerStudyMetadata;

public interface CancerStudyMetadataMapper {
    CancerStudyMetadata getCancerStudyMetadata(String studyId);
}

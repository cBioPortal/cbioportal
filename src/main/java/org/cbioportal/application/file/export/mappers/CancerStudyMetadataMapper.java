package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CancerType;

import java.util.List;

public interface CancerStudyMetadataMapper {
    CancerStudyMetadata getCancerStudyMetadata(String studyId);

    List<CancerType> getCancerTypeHierarchy(String studyId);
}

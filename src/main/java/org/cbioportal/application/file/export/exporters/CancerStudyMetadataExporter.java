package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.model.CancerStudyMetadata;

import java.util.Optional;

/**
 * Exports metadata for a cancer study
 */
public class CancerStudyMetadataExporter extends MetadataExporter<CancerStudyMetadata> {

    private final CancerStudyMetadataService cancerStudyMetadataService;

    public CancerStudyMetadataExporter(CancerStudyMetadataService cancerStudyMetadataService) {
        this.cancerStudyMetadataService = cancerStudyMetadataService;
    }

    @Override
    public String getMetaFilename(CancerStudyMetadata metadata) {
        return "meta_study.txt";
    }

    @Override
    protected Optional<CancerStudyMetadata> getMetadata(String studyId) {
        return Optional.ofNullable(cancerStudyMetadataService.getCancerStudyMetadata(studyId));
    }

}

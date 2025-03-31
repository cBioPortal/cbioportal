package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.model.CancerStudyMetadata;

import java.io.Writer;

public class CancerStudyMetadataExporter extends MetadataExporter<CancerStudyMetadata> {

    private final CancerStudyMetadataService cancerStudyMetadataService;

    public CancerStudyMetadataExporter(CancerStudyMetadataService cancerStudyMetadataService) {
        this.cancerStudyMetadataService = cancerStudyMetadataService;
    }

    @Override
    public String getMetaFilename() {
        return "meta_study.txt";
    }

    @Override
    protected CancerStudyMetadata getMetadata(String studyId) {
        return cancerStudyMetadataService.getCancerStudyMetadata(studyId);
    }

    @Override
    protected void writeMetadata(Writer writer, CancerStudyMetadata metadata) {
        new KeyValueMetadataWriter(writer).write(metadata);
    }
}

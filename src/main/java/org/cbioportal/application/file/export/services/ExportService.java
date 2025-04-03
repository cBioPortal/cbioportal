package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.exporters.CancerStudyMetadataExporter;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class ExportService implements Exporter {

    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    private final List<Exporter> exporters;

    public ExportService(
        List<Exporter> exporters
    ) {
        this.exporters = exporters;
    }

    @Transactional
    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        Optional<Exporter> cancerStudyMetadataExporterOptional = exporters.stream().filter(exporter -> exporter instanceof CancerStudyMetadataExporter).findFirst();
        if (cancerStudyMetadataExporterOptional.isEmpty()) {
            throw new RuntimeException("CancerStudyMetadataExporter is not found");
        }
        Exporter cancerStudyMetadataExporter = cancerStudyMetadataExporterOptional.get();
        if (!cancerStudyMetadataExporter.exportData(fileWriterFactory, studyId)) {
            LOG.error("No data found for studyId: {} using exporter: {}", studyId, cancerStudyMetadataExporter.getClass().getSimpleName());
            return false;
        }
        for (Exporter exporter : exporters) {
            if (exporter == cancerStudyMetadataExporter) {
                continue;
            }
            boolean exportedData = exporter.exportData(fileWriterFactory, studyId);
            LOG.debug("{} data for studyId: {} using exporter: {}",
                exportedData ? "Exported" : "No data exported",
                studyId,
                exporter.getClass().getSimpleName());
        }
        return true;
    }
}

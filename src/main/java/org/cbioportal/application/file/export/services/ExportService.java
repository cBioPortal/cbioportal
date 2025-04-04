package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        boolean atLeastOneDataFileExportedSuccesfully = false;
        for (Exporter exporter : exporters) {
            boolean exportedDataType = exporter.exportData(fileWriterFactory, studyId);
            LOG.debug("{} data for studyId: {} using exporter: {}",
                exportedDataType ? "Exported" : "No data exported",
                studyId,
                exporter.getClass().getSimpleName());
            atLeastOneDataFileExportedSuccesfully |= exportedDataType;
        }
        return atLeastOneDataFileExportedSuccesfully;
    }
}

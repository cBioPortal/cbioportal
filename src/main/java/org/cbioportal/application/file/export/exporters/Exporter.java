package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.utils.FileWriterFactory;

/**
 * Exports data to file(s) for a study
 */
public interface Exporter {

    /**
     * @param fileWriterFactory - a factory to create writers
     * @param studyId           - the study id
     * @return true - if data was exported, false - if no data was exported e.g. no data available for the study
     */
    boolean exportData(FileWriterFactory fileWriterFactory, String studyId);
}

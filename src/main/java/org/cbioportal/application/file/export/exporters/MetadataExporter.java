package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;

import java.io.IOException;
import java.io.Writer;

/**
 * Exports a metadata to a file
 *
 * @param <M> - a metadata type
 */
public abstract class MetadataExporter<M extends StudyRelatedMetadata> implements Exporter {

    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        M metadata = getMetadata(studyId);
        if (metadata == null) {
            return false;
        }
        try (Writer metaFileWriter = fileWriterFactory.newWriter(getMetaFilename())) {
            writeMetadata(metaFileWriter, metadata);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String getMetaFilename();

    protected abstract M getMetadata(String studyId);

    protected abstract void writeMetadata(Writer writer, M metadata);

}

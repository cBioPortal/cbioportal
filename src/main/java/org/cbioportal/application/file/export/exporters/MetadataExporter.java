package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;

import java.io.Writer;
import java.util.Optional;
import java.util.SequencedMap;

/**
 * Exports a metadata to a file
 *
 * @param <M> - a metadata type
 */
public abstract class MetadataExporter<M extends StudyRelatedMetadata> implements Exporter {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MetadataExporter.class);

    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        Optional<M> metadata = getMetadata(studyId);
        if (metadata.isEmpty()) {
            LOG.debug("No {} metadata available for study {}", this.getClass().getSimpleName(), studyId);
            return false;
        }
        String metaFilename = getMetaFilename(metadata.get());
        writeMetadata(fileWriterFactory, metaFilename, metadata.get());
        return true;
    }

    /**
     * Write the metadata to a file
     */
    protected void writeMetadata(FileWriterFactory fileWriterFactory, String metaFilename, M metadata) {
        try (Writer metaFileWriter = fileWriterFactory.newWriter(metaFilename)) {
            SequencedMap<String, String> metadataSeqMap = metadata.toMetadataKeyValues();
            LOG.debug("Writing {} metadata for {} study to file: {}",
                this.getClass().getSimpleName(), metadata.getCancerStudyIdentifier(), metaFilename);
            new KeyValueMetadataWriter(metaFileWriter).write(metadataSeqMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param metadata - metadata to write. Can be used to determine the unique filename
     * @return the filename to write the metadata to
     */
    public abstract String getMetaFilename(M metadata);

    /**
     * Get metadata of the datatype for a study
     *
     * @param studyId - the stable study id
     * @return metadata if available, empty otherwise
     */
    protected abstract Optional<M> getMetadata(String studyId);
}

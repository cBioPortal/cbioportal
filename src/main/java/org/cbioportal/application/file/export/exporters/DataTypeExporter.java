package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.export.writers.TsvDataWriter;
import org.cbioportal.application.file.model.GeneticDatatypeMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Writer;
import java.util.Iterator;
import java.util.Optional;
import java.util.SequencedMap;

/**
 * Export metadata and data for a specific data type (genetic alteration type + datatype).
 *
 * @param <M> - a metadata type
 * @param <D> - a data type
 */
public abstract class DataTypeExporter<M extends GeneticDatatypeMetadata, D extends Iterator<SequencedMap<String, String>> & Closeable> implements Exporter {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypeExporter.class);

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        Optional<M> metadataOptional = getMetadata(studyId);
        if (metadataOptional.isEmpty()) {
            LOG.debug("No metadata found for study {} by {} exporter. Skipping export of this datatype.", studyId, getClass().getSimpleName());
            return false;
        }
        M metadata = metadataOptional.get();
        String metaFilename = getMetaFilename(metadata);
        String dataFilename = getDataFilename(metadata);
        writeMetadata(fileWriterFactory, metaFilename, metadata, dataFilename);
        writeData(fileWriterFactory, metadata, dataFilename);
        LOG.debug("Data (genetic alteration type: {}, datatype: {}) has been exported for study {} by {} exporter.", metadata.getGeneticAlterationType(), metadata.getDatatype(), studyId, getClass().getSimpleName());
        return true;
    }

    /**
     * Get the filename for the data file.
     *
     * @param metadata - metadata for the datatype. Can be used to generate the unique filename.
     * @return the filename for the data file
     */
    public String getDataFilename(M metadata) {
        return "data_" + metadata.getGeneticAlterationType().toLowerCase() + "_" + metadata.getDatatype().toLowerCase() + ".txt";
    }

    /**
     * Get the filename for the metadata file.
     *
     * @param metadata - metadata for the datatype. Can be used to generate the unique filename.
     * @return the filename for the metadata file
     */
    public String getMetaFilename(M metadata) {
        return "meta_" + metadata.getGeneticAlterationType().toLowerCase() + "_" + metadata.getDatatype().toLowerCase() + ".txt";
    }

    /**
     * Write metadata to a file.
     */
    protected void writeMetadata(FileWriterFactory fileWriterFactory, String metaFilename, M metadata, String dataFilename) {
        try (Writer metaFileWriter = fileWriterFactory.newWriter(metaFilename)) {
            SequencedMap<String, String> metadataSeqMap = metadata.toMetadataKeyValues();
            LOG.debug("Writing metadata (genetic alteration type: {}, datatype: {}) to file: {}",
                metadata.getGeneticAlterationType(), metadata.getDatatype(), metaFilename);
            metadataSeqMap.put("data_filename", dataFilename);
            new KeyValueMetadataWriter(metaFileWriter).write(metadataSeqMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write data to a file.
     */
    protected void writeData(FileWriterFactory fileWriterFactory, M metadata, String dataFilename) {
        try (D data = getData(metadata.getCancerStudyIdentifier()); Writer dataFileWriter = fileWriterFactory.newWriter(dataFilename)) {
            LOG.debug("Writing data for study {} (genetic alteration type: {}, datatype: {}) to file: {}",
                metadata.getCancerStudyIdentifier(), metadata.getGeneticAlterationType(), metadata.getDatatype(), dataFilename);
            new TsvDataWriter(dataFileWriter).write(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get metadata for the datatype of a specific study.
     *
     * @param studyId - study stable identifier
     * @return metadata for the datatype of the study if available
     */
    protected abstract Optional<M> getMetadata(String studyId);

    /**
     * Get the data for the datatype of a specific study.
     *
     * @param studyId - study stable identifier
     * @return data for the datatype of the study
     */
    protected abstract D getData(String studyId);

}

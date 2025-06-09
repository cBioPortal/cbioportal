package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.model.GeneticDatatypeMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;

import static org.cbioportal.application.file.export.writers.WriterHelper.writeMetadata;
import static org.cbioportal.application.file.export.writers.WriterHelper.writeData;

/**
 * Export metadata and data for a specific data type (genetic alteration type + datatype).
 *
 * @param <M> - a metadata type
 * @param <D> - a data type
 */
public abstract class DataTypeExporter<M extends GeneticDatatypeMetadata, D extends Iterator<SequencedMap<String, String>> & Closeable> implements Exporter {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypeExporter.class);

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
        Optional<M> metadataOptional = getMetadata(exportDetails.getStudyId(), exportDetails.getSampleIds());
        if (metadataOptional.isEmpty()) {
            LOG.debug("No metadata found for study {} by {} exporter. Skipping export of this datatype.", exportDetails.getExportWithStudyId(), getClass().getSimpleName());
            return false;
        }
        M metadata = metadataOptional.get();
        if (!metadata.getCancerStudyIdentifier().equals(exportDetails.getStudyId())) {
            throw new IllegalStateException("Metadata study ID (" + metadata.getGeneticAlterationType() + ") does not match the provided study ID (" + exportDetails.getStudyId() + ").");
        }
        String metaFilename = getMetaFilename(metadata);
        String dataFilename = getDataFilename(metadata);
        writeMetadata(fileWriterFactory, metaFilename, metadata, dataFilename, exportDetails);
        try(D data = getData(exportDetails.getStudyId(), exportDetails.getSampleIds())) {
            writeData(fileWriterFactory, dataFilename, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.debug("Data (genetic alteration type: {}, datatype: {}) has been exported for study {} by {} exporter.", metadata.getGeneticAlterationType(), metadata.getDatatype(), exportDetails.getStudyId(), getClass().getSimpleName());
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
     * Get metadata for the datatype of a specific study.
     *
     * @param studyId - study stable identifier
     * @param sampleIds - set of sample IDs to filter the metadata; can be null
     * @return metadata for the datatype of the study if available
     */
    protected abstract Optional<M> getMetadata(String studyId, Set<String> sampleIds);

    /**
     * Get the data for the datatype of a specific study.
     *
     * @param studyId - study stable identifier
     * @param sampleIds - set of sample IDs to filter the data; can be null
     * @return data for the datatype of the study
     */
    protected abstract D getData(String studyId, Set<String> sampleIds);

}

package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.export.writers.TsvDataWriter;
import org.cbioportal.application.file.model.GeneticDatatypeMetadata;
import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Writer;
import java.util.Iterator;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;

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
            LOG.debug("No metadata found for study {} by {} exporter. Skipping export of this datatype.", exportDetails.getExportAsStudyId(), getClass().getSimpleName());
            return false;
        }
        M metadata = metadataOptional.get();
        if (!metadata.getCancerStudyIdentifier().equals(exportDetails.getStudyId())) {
            throw new IllegalStateException("Metadata study ID (" + metadata.getGeneticAlterationType() + ") does not match the provided study ID (" + exportDetails.getStudyId() + ").");
        }
        String metaFilename = getMetaFilename(metadata);
        String dataFilename = getDataFilename(metadata);
        writeMetadata(fileWriterFactory, metaFilename, metadata, dataFilename, exportDetails);
        writeData(fileWriterFactory, metadata, dataFilename, exportDetails);
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
     * Write metadata to a file.
     */
    protected void writeMetadata(FileWriterFactory fileWriterFactory, String metaFilename, M metadata, String dataFilename, ExportDetails exportDetails) {
        try (Writer metaFileWriter = fileWriterFactory.newWriter(metaFilename)) {
            SequencedMap<String, String> metadataSeqMap = metadata.toMetadataKeyValues();
            LOG.debug("Writing metadata (genetic alteration type: {}, datatype: {}) to file: {}",
                metadata.getGeneticAlterationType(), metadata.getDatatype(), metaFilename);
            if (exportDetails.getExportAsStudyId() != null) {
                LOG.debug("Exporting {} metadata for study {} as study {}",
                    this.getClass().getSimpleName(), metadata.getCancerStudyIdentifier(), exportDetails.getExportAsStudyId());
                metadataSeqMap.putAll(((StudyRelatedMetadata) exportDetails::getExportAsStudyId).toMetadataKeyValues());
            }
            if (exportDetails.getSampleIds() != null && metadataSeqMap.containsKey("description")) {
                LOG.debug("Updating description for {} metadata for study {} to include sample count",
                    this.getClass().getSimpleName(), exportDetails.getStudyId());
                metadataSeqMap.put("description", "Selection of " + exportDetails.getSampleIds().size() + " samples. Original data description:" + metadataSeqMap.get("description"));
            }
            metadataSeqMap.put("data_filename", dataFilename);
            new KeyValueMetadataWriter(metaFileWriter).write(metadataSeqMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write data to a file.
     */
    protected void writeData(FileWriterFactory fileWriterFactory, M metadata, String dataFilename, ExportDetails exportDetails) {
        try (D data = getData(metadata.getCancerStudyIdentifier(), exportDetails.getSampleIds()); Writer dataFileWriter = fileWriterFactory.newWriter(dataFilename)) {
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

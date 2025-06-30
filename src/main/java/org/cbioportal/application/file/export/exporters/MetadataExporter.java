package org.cbioportal.application.file.export.exporters;

import java.io.Writer;
import java.util.Optional;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.ExportException;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;

/**
 * Exports a metadata to a file
 *
 * @param <M> - a metadata type
 */
public abstract class MetadataExporter<M extends StudyRelatedMetadata> implements Exporter {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MetadataExporter.class);
  public static final String DESCRIPTION = "description";

  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    Optional<M> metadata = getMetadata(exportDetails.getStudyId());
    if (metadata.isEmpty()) {
      LOG.debug(
          "No {} metadata available for study {}",
          this.getClass().getSimpleName(),
          exportDetails.getStudyId());
      return false;
    }
    String metaFilename = getMetaFilename(metadata.get());
    writeMetadata(fileWriterFactory, metaFilename, metadata.get(), exportDetails);
    return true;
  }

  /** Write the metadata to a file */
  protected void writeMetadata(
      FileWriterFactory fileWriterFactory,
      String metaFilename,
      M metadata,
      ExportDetails exportDetails) {
    try (Writer metaFileWriter = fileWriterFactory.newWriter(metaFilename)) {
      SequencedMap<String, String> metadataSeqMap = metadata.toMetadataKeyValues();
      LOG.debug(
          "Writing {} metadata for {} study to file: {}",
          this.getClass().getSimpleName(),
          metadata.getCancerStudyIdentifier(),
          metaFilename);
      updateMetadata(
          exportDetails, metadataSeqMap); // update the study metadata with the export details
      new KeyValueMetadataWriter(metaFileWriter).write(metadataSeqMap);
    } catch (Exception e) {
      throw new ExportException(
          "Error while writing metadata for study "
              + exportDetails.getStudyId()
              + " to file "
              + metaFilename,
          e);
    }
  }

  protected void updateMetadata(
      ExportDetails exportDetails, SequencedMap<String, String> metadataSeqMap) {
    if (exportDetails.getExportWithStudyId() != null) {
      LOG.debug(
          "Exporting {} metadata for study {} as study {}",
          this.getClass().getSimpleName(),
          exportDetails.getStudyId(),
          exportDetails.getExportWithStudyId());
      metadataSeqMap.putAll(
          ((StudyRelatedMetadata) exportDetails::getExportWithStudyId).toMetadataKeyValues());
    }
    if (exportDetails.getSampleIds() != null && metadataSeqMap.containsKey(DESCRIPTION)) {
      LOG.debug(
          "Updating description for {} metadata for study {} to include sample count",
          this.getClass().getSimpleName(),
          exportDetails.getStudyId());
      metadataSeqMap.put(
          DESCRIPTION,
          "Selection of samples. Original data description: " + metadataSeqMap.get(DESCRIPTION));
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

package org.cbioportal.application.file.export.writers;

import java.io.Writer;
import java.util.Iterator;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.ExportException;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.model.GeneticDatatypeMetadata;
import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriterHelper {
  private static final Logger LOG = LoggerFactory.getLogger(WriterHelper.class);

  private WriterHelper() {
    // Prevent instantiation
  }

  /** Write metadata to a file. */
  public static <M extends GeneticDatatypeMetadata> void writeMetadata(
      FileWriterFactory fileWriterFactory,
      String metaFilename,
      M metadata,
      String dataFilename,
      ExportDetails exportDetails) {
    try (Writer metaFileWriter = fileWriterFactory.newWriter(metaFilename)) {
      SequencedMap<String, String> metadataSeqMap = metadata.toMetadataKeyValues();
      LOG.debug(
          "Writing metadata (genetic alteration type: {}, datatype: {}) to file: {}",
          metadata.getGeneticAlterationType(),
          metadata.getDatatype(),
          metaFilename);
      if (exportDetails.getExportWithStudyId() != null) {
        LOG.debug(
            "Exporting metadata for study {} as study {}",
            metadata.getCancerStudyIdentifier(),
            exportDetails.getExportWithStudyId());
        metadataSeqMap.putAll(
            ((StudyRelatedMetadata) exportDetails::getExportWithStudyId).toMetadataKeyValues());
      }
      metadataSeqMap.put("data_filename", dataFilename);
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

  /** Write data to a file. */
  public static <D extends Iterator<SequencedMap<String, String>>> void writeData(
      FileWriterFactory fileWriterFactory, String dataFilename, D data) {
    try (Writer dataFileWriter = fileWriterFactory.newWriter(dataFilename)) {
      new TsvDataWriter(dataFileWriter).write(data);
    } catch (Exception e) {
      throw new ExportException(
          "Error while writing data to file "
              + dataFilename
              + " for data type "
              + data.getClass().getSimpleName()
              + ".",
          e);
    }
  }
}

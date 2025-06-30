package org.cbioportal.application.file.export.exporters;

import java.util.List;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;

/**
 * Export metadata and data for a genetic profile of certain data type (genetic alteration type +
 * datatype).
 */
public abstract class GeneticProfileDatatypeExporter implements Exporter {

  private static final Logger LOG =
      org.slf4j.LoggerFactory.getLogger(GeneticProfileDatatypeExporter.class);
  private final GeneticProfileService geneticProfileService;

  protected GeneticProfileDatatypeExporter(GeneticProfileService geneticProfileService) {
    this.geneticProfileService = geneticProfileService;
  }

  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    List<GeneticProfileDatatypeMetadata> geneticProfiles =
        geneticProfileService.getGeneticProfiles(
            exportDetails.getStudyId(),
            exportDetails.getSampleIds(),
            getGeneticAlterationType(),
            getDatatype());
    boolean exported = false;
    for (GeneticProfileDatatypeMetadata metadata : geneticProfiles) {
      if (metadata.getPatientLevel() != null && metadata.getPatientLevel()) {
        LOG.debug("Skipping unsupported patient-level genetic profile: {}", metadata.getStableId());
        continue;
      }
      exported |= composeExporterFor(metadata).exportData(fileWriterFactory, exportDetails);
    }
    return exported;
  }

  protected abstract Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata);

  protected abstract String getGeneticAlterationType();

  protected abstract String getDatatype();

  protected abstract static class GeneticProfileExporter
      extends DataTypeExporter<
          GeneticProfileDatatypeMetadata, CloseableIterator<SequencedMap<String, String>>> {
    @Override
    public String getDataFilename(GeneticProfileDatatypeMetadata metadata) {
      return "data_"
          + metadata.getGeneticAlterationType().toLowerCase()
          + "_"
          + metadata.getDatatype().toLowerCase()
          + "_"
          + metadata.getGeneticDatatypeStableId().toLowerCase()
          + ".txt";
    }

    @Override
    public String getMetaFilename(GeneticProfileDatatypeMetadata metadata) {
      return "meta_"
          + metadata.getGeneticAlterationType().toLowerCase()
          + "_"
          + metadata.getDatatype().toLowerCase()
          + "_"
          + metadata.getGeneticDatatypeStableId().toLowerCase()
          + ".txt";
    }
  }
}

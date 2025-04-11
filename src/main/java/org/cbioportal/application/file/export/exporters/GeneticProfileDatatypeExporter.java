package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.FileWriterFactory;

import java.util.List;
import java.util.SequencedMap;

/**
 * Export metadata and data for a genetic profile of certain data type (genetic alteration type + datatype).
 */
public abstract class GeneticProfileDatatypeExporter implements Exporter {

    private final GeneticProfileService geneticProfileService;

    public GeneticProfileDatatypeExporter(GeneticProfileService geneticProfileService) {
        this.geneticProfileService = geneticProfileService;
    }

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        List<GeneticProfileDatatypeMetadata> geneticProfiles = geneticProfileService.getGeneticProfiles(studyId, getGeneticAlterationType(), getDatatype());
        boolean exported = false;
        for (GeneticProfileDatatypeMetadata metadata : geneticProfiles) {
            exported |= composeExporterFor(metadata).exportData(fileWriterFactory, studyId);
        }
        return exported;
    }

    protected abstract Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata);

    protected abstract String getGeneticAlterationType();

    protected abstract String getDatatype();

    protected abstract class GeneticProfileExporter extends DataTypeExporter<GeneticProfileDatatypeMetadata, CloseableIterator<SequencedMap<String, String>>> {
        @Override
        public String getDataFilename(GeneticProfileDatatypeMetadata metadata) {
            return "data_" + metadata.getGeneticAlterationType().toLowerCase()
                + "_" + metadata.getDatatype().toLowerCase()
                + "_" + metadata.getGeneticDatatypeStableId().toLowerCase() + ".txt";
        }

        @Override
        public String getMetaFilename(GeneticProfileDatatypeMetadata metadata) {
            return "meta_" + metadata.getGeneticAlterationType().toLowerCase()
                + "_" + metadata.getDatatype().toLowerCase()
                + "_" + metadata.getGeneticDatatypeStableId().toLowerCase() + ".txt";
        }
    }
}
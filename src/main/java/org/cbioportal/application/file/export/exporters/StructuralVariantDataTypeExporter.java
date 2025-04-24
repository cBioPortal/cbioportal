package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.StructuralVariantService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.Optional;

public class StructuralVariantDataTypeExporter extends GeneticProfileDatatypeExporter {

    private final StructuralVariantService structuralVariantService;

    public StructuralVariantDataTypeExporter(GeneticProfileService geneticProfileService, StructuralVariantService structuralVariantService) {
        super(geneticProfileService);
        this.structuralVariantService = structuralVariantService;
    }

    @Override
    protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
        return new SVGeneticProfileExporter(metadata);
    }

    @Override
    protected String getGeneticAlterationType() {
        return "STRUCTURAL_VARIANT";
    }

    @Override
    protected String getDatatype() {
        return "SV";
    }

    private class SVGeneticProfileExporter extends GeneticProfileExporter {

        private final GeneticProfileDatatypeMetadata metadata;

        public SVGeneticProfileExporter(GeneticProfileDatatypeMetadata metadata) {
            this.metadata = metadata;
        }

        @Override
        protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId) {
            return Optional.of(metadata);
        }

        @Override
        protected Table getData(String studyId) {
            CloseableIterator<StructuralVariant> structuralVariantData = structuralVariantService.getStructuralVariants(metadata.getStableId());
            return new Table(structuralVariantData, StructuralVariant.getHeader());
        }
    }
}

package org.cbioportal.application.file.export.exporters;

import java.util.Optional;
import java.util.Set;
import org.cbioportal.application.file.services.GeneticProfileService;
import org.cbioportal.application.file.services.StructuralVariantService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;

public class StructuralVariantDataTypeExporter extends GeneticProfileDatatypeExporter {

  private final StructuralVariantService structuralVariantService;

  public StructuralVariantDataTypeExporter(
      GeneticProfileService geneticProfileService,
      StructuralVariantService structuralVariantService) {
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
    protected Optional<GeneticProfileDatatypeMetadata> getMetadata(
        String studyId, Set<String> sampleIds) {
      return Optional.of(metadata);
    }

    @Override
    protected Table getData(String studyId, Set<String> sampleIds) {
      CloseableIterator<StructuralVariant> structuralVariantData =
          structuralVariantService.getStructuralVariants(metadata.getStableId(), sampleIds);
      return new Table(structuralVariantData, StructuralVariant.getHeader());
    }
  }
}

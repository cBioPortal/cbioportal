package org.cbioportal.application.file.export.exporters;

import java.util.LinkedHashMap;
import java.util.function.Function;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

public abstract class GenericAssayDatatypeExporter extends GeneticAlterationTsvExporter {
  protected GenericAssayDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  private static final LinkedHashMap<String, Function<GeneticProfileData, String>> ROW =
      new LinkedHashMap<>();

  static {
    ROW.put(
        "ENTITY_STABLE_ID",
        data -> data.getGeneticEntity() == null ? null : data.getGeneticEntity().getStableId());
  }

  @Override
  protected LinkedHashMap<String, Function<GeneticProfileData, String>> getRowMappers() {
    return ROW;
  }

  @Override
  protected void setGenericEntitiesMetaProperties(GeneticProfileDatatypeMetadata metadata) {
    metadata.setGenericEntitiesMetaProperties(
        geneticProfileDataService.getDistinctGenericEntityMetaPropertyNames(
            metadata.getStableId()));
  }

  protected String getGeneticAlterationType() {
    return "GENERIC_ASSAY";
  }
}

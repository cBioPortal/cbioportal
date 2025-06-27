package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public abstract class MrnaExpressionDatatypeExporter extends GeneSampleWideTableDatatypeExporter {
  public MrnaExpressionDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getGeneticAlterationType() {
    return "MRNA_EXPRESSION";
  }
}

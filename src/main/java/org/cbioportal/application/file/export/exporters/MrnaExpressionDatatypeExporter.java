package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.services.GeneticProfileDataService;
import org.cbioportal.application.file.services.GeneticProfileService;

public abstract class MrnaExpressionDatatypeExporter extends GeneSampleWideTableDatatypeExporter {
  protected MrnaExpressionDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getGeneticAlterationType() {
    return "MRNA_EXPRESSION";
  }
}

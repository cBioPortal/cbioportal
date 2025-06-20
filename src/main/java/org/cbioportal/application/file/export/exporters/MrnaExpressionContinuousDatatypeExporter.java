package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public class MrnaExpressionContinuousDatatypeExporter extends MrnaExpressionDatatypeExporter {
  public MrnaExpressionContinuousDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getDatatype() {
    return "CONTINUOUS";
  }
}

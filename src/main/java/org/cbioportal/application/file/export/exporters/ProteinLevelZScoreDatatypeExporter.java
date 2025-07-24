package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public class ProteinLevelZScoreDatatypeExporter extends ProteinLevelDatatypeExporter {
  public ProteinLevelZScoreDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getDatatype() {
    return "Z-SCORE";
  }
}

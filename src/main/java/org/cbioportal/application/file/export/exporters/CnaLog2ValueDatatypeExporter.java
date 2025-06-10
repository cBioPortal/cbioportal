package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public class CnaLog2ValueDatatypeExporter extends GeneSampleWideTableDatatypeExporter {
  public CnaLog2ValueDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getGeneticAlterationType() {
    return "COPY_NUMBER_ALTERATION";
  }

  @Override
  protected String getDatatype() {
    return "LOG2-VALUE";
  }
}

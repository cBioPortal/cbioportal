package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.services.GeneticProfileDataService;
import org.cbioportal.application.file.services.GeneticProfileService;

public class CnaDiscreteDatatypeExporter extends GeneSampleWideTableDatatypeExporter {
  public CnaDiscreteDatatypeExporter(
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
    return "DISCRETE";
  }
}

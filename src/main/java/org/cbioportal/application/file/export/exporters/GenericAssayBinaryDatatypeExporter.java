package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.services.GeneticProfileDataService;
import org.cbioportal.application.file.services.GeneticProfileService;

public class GenericAssayBinaryDatatypeExporter extends GenericAssayDatatypeExporter {
  public GenericAssayBinaryDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  @Override
  protected String getDatatype() {
    return "BINARY";
  }
}

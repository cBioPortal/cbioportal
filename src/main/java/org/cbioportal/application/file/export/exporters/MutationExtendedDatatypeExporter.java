package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;

public class MutationExtendedDatatypeExporter extends MafDataTypeExporter {
  public MutationExtendedDatatypeExporter(
      GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
    super(geneticProfileService, mafRecordService);
  }

  @Override
  protected String getGeneticAlterationType() {
    return "MUTATION_EXTENDED";
  }
}

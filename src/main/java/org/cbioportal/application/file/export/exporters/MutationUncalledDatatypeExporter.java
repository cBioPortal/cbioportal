package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.services.GeneticProfileService;
import org.cbioportal.application.file.services.MafRecordService;

public class MutationUncalledDatatypeExporter extends MafDataTypeExporter {
  public MutationUncalledDatatypeExporter(
      GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
    super(geneticProfileService, mafRecordService);
  }

  @Override
  protected String getGeneticAlterationType() {
    return "MUTATION_UNCALLED";
  }
}

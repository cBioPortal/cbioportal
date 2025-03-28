package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;

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

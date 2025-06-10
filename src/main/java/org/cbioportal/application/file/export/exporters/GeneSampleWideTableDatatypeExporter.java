package org.cbioportal.application.file.export.exporters;

import java.util.LinkedHashMap;
import java.util.function.Function;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

public abstract class GeneSampleWideTableDatatypeExporter extends GeneticAlterationTsvExporter {

  public GeneSampleWideTableDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService, geneticProfileDataService);
  }

  private static final LinkedHashMap<String, Function<GeneticProfileData, String>> GENE_ROW =
      new LinkedHashMap<>();

  static {
    GENE_ROW.put(
        "Hugo_Symbol", data -> data.getGene() == null ? null : data.getGene().getHugoGeneSymbol());
    GENE_ROW.put(
        "Entrez_Gene_Id",
        data ->
            data.getGene() == null || data.getGene().getEntrezGeneId() == null
                ? null
                : data.getGene().getEntrezGeneId().toString());
  }

  @Override
  protected LinkedHashMap<String, Function<GeneticProfileData, String>> getRowMappers() {
    return GENE_ROW;
  }

  @Override
  protected void setGenericEntitiesMetaProperties(GeneticProfileDatatypeMetadata metadata) {}
}

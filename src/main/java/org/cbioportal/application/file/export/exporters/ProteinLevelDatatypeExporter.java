package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.function.Function;

public abstract class ProteinLevelDatatypeExporter extends GeneticAlterationTsvExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ProteinLevelDatatypeExporter.class);

    public ProteinLevelDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService, geneticProfileDataService);
    }

    @Override
    protected String getGeneticAlterationType() {
        return "PROTEIN_LEVEL";
    }

    private static final LinkedHashMap<String, Function<GeneticProfileData, String>> ROW = new LinkedHashMap<>();

    static {
        ROW.put("Composite.Element.REF", data -> {
            if (data.getGene() == null) {
                return null;
            }
            String hugoGeneSymbol = data.getGene().getHugoGeneSymbol();
            if ("phosphoprotein".equals(data.getGene().getType())) {
                String[] parts = hugoGeneSymbol.split("_");
                //first part is actual hugo gene symbol. the second part is the phosphosite. example of composed string: PDK1|PDK1_pS24
                if (parts.length != 2) {
                    throw new IllegalStateException("Unexpected format for phosphoprotein: " + hugoGeneSymbol);
                }
                String hgs = parts[0];
                String phosphosite = parts[1];
                if (phosphosite.charAt(0) != 'p' && phosphosite.charAt(0) != 'P') {
                    LOG.warn("Unexpected format for phosphosite: {}", phosphosite);
                    return hugoGeneSymbol + "|" + hugoGeneSymbol;
                }
                return hgs + "|" + hgs + "_p" + phosphosite.substring(1);
            } else {
                return hugoGeneSymbol + "|" + hugoGeneSymbol;
            }
        });
    }

    @Override
    protected LinkedHashMap<String, Function<GeneticProfileData, String>> getRowMappers() {
        return ROW;
    }

    @Override
    protected void setGenericEntitiesMetaProperties(GeneticProfileDatatypeMetadata metadata) {
    }
}

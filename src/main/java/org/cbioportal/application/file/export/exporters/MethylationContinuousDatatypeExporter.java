package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public class MethylationContinuousDatatypeExporter extends GeneSampleWideTableDatatypeExporter {
    public MethylationContinuousDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService, geneticProfileDataService);
    }

    @Override
    protected String getGeneticAlterationType() {
        return "METHYLATION";
    }

    @Override
    protected String getDatatype() {
        return "CONTINUOUS";
    }
}

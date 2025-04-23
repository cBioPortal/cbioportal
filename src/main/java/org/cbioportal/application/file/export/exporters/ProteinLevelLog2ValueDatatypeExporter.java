package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;

public class ProteinLevelLog2ValueDatatypeExporter extends ProteinLevelDatatypeExporter {
    public ProteinLevelLog2ValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService, geneticProfileDataService);
    }

    @Override
    protected String getDatatype() {
        return "LOG2-VALUE";
    }
}

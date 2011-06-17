package org.mskcc.portal.tool.bundle;

import org.mskcc.portal.model.GeneticAlterationType;

public class GeneRequest {
    private String geneSymbol;
    private String genomicProfileId;
    private GeneticAlterationType alterationType;

    public GeneRequest(String geneSymbol, String genomicProfileId,
                       GeneticAlterationType alterationType) {
        this.geneSymbol = geneSymbol;
        this.genomicProfileId = genomicProfileId;
        this.alterationType = alterationType;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGenomicProfileId() {
        return genomicProfileId;
    }

    public GeneticAlterationType getAlterationType() {
        return alterationType;
    }
}

package org.cbioportal.model;

import jakarta.validation.Valid;
import java.io.Serializable;

public class StructuralVariantQuery implements Serializable {
    
    @Valid 
    private StructuralVariantGeneSubQuery gene1;

    @Valid 
    private StructuralVariantGeneSubQuery gene2;

    public StructuralVariantQuery(
        StructuralVariantGeneSubQuery gene1, 
        StructuralVariantGeneSubQuery gene2
    ) {
        this.gene1 = gene1;
        this.gene2 = gene2;
    }

    public StructuralVariantQuery() {
        // Needed by jackson
    }

    public StructuralVariantGeneSubQuery getGene1() {
        return gene1;
    }

    public void setGene1(StructuralVariantGeneSubQuery gene1) {
        this.gene1 = gene1;
    }

    public StructuralVariantGeneSubQuery getGene2() {
        return gene2;
    }

    public void setGene2(StructuralVariantGeneSubQuery gene2) {
        this.gene2 = gene2;
    }
    
}

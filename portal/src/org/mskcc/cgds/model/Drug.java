package org.mskcc.cgds.model;

// CREATED BY P. MANKOO: 27 JULY, 2009

/**
 * Clas for drug information
 */
public class Drug {
    private CanonicalGene gene;
    private String drugType;
    private long drugId;

    public long getdrugId() {
        return drugId;
    }

    public void setDrugId(long drugId) {
        this.drugId = drugId;
    }

    public CanonicalGene getGene() {
        return gene;
    }

    public void setGene(CanonicalGene gene) {
        this.gene = gene;
    }

    public String getDrugType() {
        return drugType;
    }

    public void setDrugType(String drugType) {
        this.drugType = drugType;
    }

    @Override
    public String toString() {
	return drugType;
    }
}

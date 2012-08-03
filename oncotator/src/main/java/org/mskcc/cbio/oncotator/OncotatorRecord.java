package org.mskcc.cbio.oncotator;

/**
 * Encapsulate a Single Record from Oncotator.
 */
public class OncotatorRecord {
    private String key;
    private String gene;
    private String genomeChange;
    private String proteinChange;
    private String variantClassification;
    private int exonAffected;
    private String cosmicOverlappingMutations;
    private String dbSnpRs;

    public OncotatorRecord (String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getVariantClassification() {
        return variantClassification;
    }

    public void setVariantClassification(String variantClassification) {
        this.variantClassification = variantClassification;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getGenomeChange() {
        return genomeChange;
    }

    public void setGenomeChange(String genomeChange) {
        this.genomeChange = genomeChange;
    }

    public int getExonAffected() {
        return exonAffected;
    }

    public void setExonAffected(int exonAffected) {
        this.exonAffected = exonAffected;
    }

    public String getCosmicOverlappingMutations() {
        return cosmicOverlappingMutations;
    }

    public void setCosmicOverlappingMutations(String cosmicOverlappingMutations) {
        this.cosmicOverlappingMutations = cosmicOverlappingMutations;
    }

    public String getDbSnpRs() {
        return dbSnpRs;
    }

    public void setDbSnpRs(String dbSnpRs) {
        this.dbSnpRs = dbSnpRs;
    }
}

package org.mskcc.cgds.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ExtendedMutation {
    private int geneticProfileId;
    private String caseId;
    private long entrezGeneId;
    private CanonicalGene gene;
    private String geneSymbol;
    private String center;
    private String sequencer;
    private String mutationStatus;
    private String validationStatus;
    private String chr;
    private long startPosition;
    private long endPosition;
    private String aminoAcidChange;
    private String mutationType;
    private String functionalImpactScore;
    private String linkXVar;
    private String linkPdb;
    private String linkMsa;

    public ExtendedMutation() {
    }

    /**
     * Construct an ExtendedMutation with the fields needed for testing.
     * Include mutationType mutationStatus ValidationStatus and EntrezGeneId.
     */
    public ExtendedMutation(long entrezGeneId, String validationStatus, String mutationStatus,
                            String mutationType) {
        this.entrezGeneId = entrezGeneId;
        this.mutationStatus = mutationStatus;
        this.validationStatus = validationStatus;
        this.mutationType = mutationType;
    }

    public ExtendedMutation(CanonicalGene gene, String validationStatus, String mutationStatus,
                            String mutationType) {
        this.entrezGeneId = gene.getEntrezGeneId();
        this.geneSymbol = gene.getHugoGeneSymbol();
        this.mutationStatus = mutationStatus;
        this.validationStatus = validationStatus;
        this.mutationType = mutationType;
    }

    public int getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(int geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMutationStatus() {
        return mutationStatus;
    }

    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getFunctionalImpactScore() {
        return functionalImpactScore;
    }

    public void setFunctionalImpactScore(String fImpact) {
        this.functionalImpactScore = fImpact;
    }

    public String getLinkXVar() {
        return linkXVar;
    }

    public void setLinkXVar(String linkXVar) {
        this.linkXVar = linkXVar;
    }

    public String getLinkPdb() {
        return linkPdb;
    }

    public void setLinkPdb(String linkPdb) {
        this.linkPdb = linkPdb;
    }

    public String getLinkMsa() {
        return linkMsa;
    }

    public void setLinkMsa(String linkMsa) {
        this.linkMsa = linkMsa;
    }

    public String getSequencer() {
        return sequencer;
    }

    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGene(CanonicalGene gene) {
        this.gene = gene;
    }

    public CanonicalGene getGene() {
        return gene;
    }

    public String keyFieldsToString() {
        return new ToStringBuilder(this).
                append("entrezGeneId", entrezGeneId).
                append("mutationStatus", mutationStatus).
                append("validationStatus", validationStatus).
                append("mutationType", mutationType).
                toString();
    }

}

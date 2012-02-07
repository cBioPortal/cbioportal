package org.mskcc.cgds.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Encapsules Details regarding a Single Mutation.
 *
 * @author Ethan Cerami.
 */
public class ExtendedMutation {
    private static final String GERMLINE = "germline";

    private int geneticProfileId;
    private String caseId;
    private CanonicalGene gene;
    private String sequencingCenter;
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
     * Constructor.
     *
     * @param gene              Gene Object.
     * @param validationStatus  Validation Status,  e.g. Valid or Unknown.
     * @param mutationStatus    Mutation Status, e.g. Somatic or Germline.
     * @param mutationType      Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     */
    public ExtendedMutation(CanonicalGene gene, String validationStatus, String mutationStatus,
                            String mutationType) {
        this.gene = gene;
        this.mutationStatus = mutationStatus;
        this.validationStatus = validationStatus;
        this.mutationType = mutationType;
    }

    /**
     * Sets the Sequencing Center which performed the sequencing.
     * @param center sequencing center, e.g. WashU, Broad, etc.
     */
    public void setSequencingCenter(String center) {
        this.sequencingCenter = center;
    }

    /**
     * Gets the Sequencing Center which performed the sequencing.
     * @return sequencing center, e.g. WashU, Broad, etc.
     */
    public String getSequencingCenter() {
        return sequencingCenter;
    }

    /**
     * Gets the Mutations Status, e.g. Somatic or Germline.
     * @return mutation status, e.g. Somatic or Germline.
     */
    public String getMutationStatus() {
        return mutationStatus;
    }

    @JsonIgnore
    public boolean isGermlineMutation() {
        return getMutationStatus() != null && getMutationStatus().equalsIgnoreCase(GERMLINE);
    }

    /**
     * Sets the Mutation Status, e.g. Somatic or Germline.
     * @param mutationStatus mutation status, e.g. Somatic or Germline.
     */
    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    /**
     * Sets the Validation Status, e.g. Valid or Unknown.
     * @param validationStatus validation status, e.g. Valid or Unknown.
     */
    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    /**
     * Gets the Validation Status, e.g. Valid or Unknown.
     * @return validation status, e.g. Valid or Unknown.
     */
    public String getValidationStatus() {
        return validationStatus;
    }

    /**
     * Sets the Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     * @param mutationType mutation type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     */
    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    /**
     * Gets the Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     * @return mutation type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     */
    public String getMutationType() {
        return mutationType;
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

    @JsonIgnore
    public void setGene(CanonicalGene gene) {
        this.gene = gene;
    }

    @JsonIgnore
    public CanonicalGene getGene() {
        return gene;
    }

    @JsonIgnore
    public long getEntrezGeneId() {
        return gene.getEntrezGeneId();
    }

    @JsonIgnore
    public String getGeneSymbol() {
        return gene.getHugoGeneSymbolAllCaps();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

package org.mskcc.cgds.model;

public class DrugInteraction {
    private String drug;
    private long targetGene;
    private String interactionType;
    private String dataSource;
    private String experimentTypes;
    private String pubMedIDs;

    public DrugInteraction() {
    }

    public DrugInteraction(String drug,
                           Integer targetGene,
                           String interactionType,
                           String dataSource,
                           String experimentTypes,
                           String pubMedIDs) {

        this.drug = drug;
        this.targetGene = targetGene;
        this.interactionType = interactionType;
        this.dataSource = dataSource;
        this.experimentTypes = experimentTypes;
        this.pubMedIDs = pubMedIDs;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }

    public long getTargetGene() {
        return targetGene;
    }

    public void setTargetGene(long targetGene) {
        this.targetGene = targetGene;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getExperimentTypes() {
        return experimentTypes;
    }

    public void setExperimentTypes(String experimentTypes) {
        this.experimentTypes = experimentTypes;
    }

    public String getPubMedIDs() {
        return pubMedIDs;
    }

    public void setPubMedIDs(String pubMedIDs) {
        this.pubMedIDs = pubMedIDs;
    }
}

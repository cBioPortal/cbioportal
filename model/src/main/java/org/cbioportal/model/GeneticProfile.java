package org.cbioportal.model;

import java.io.Serializable;

public class GeneticProfile implements Serializable {

    // Copied from org.mskcc.cbio.portal.model.GeneticAlterationType, if you alter this,
    // don't forget to change the original one too
    public enum GeneticAlterationType {
        MUTATION_EXTENDED,
        FUSION,
        STRUCTURAL_VARIANT,
        COPY_NUMBER_ALTERATION,
        MICRO_RNA_EXPRESSION,
        MRNA_EXPRESSION,
        MRNA_EXPRESSION_NORMALS,
        RNA_EXPRESSION,
        METHYLATION,
        METHYLATION_BINARY,
        PHOSPHORYLATION,
        PROTEIN_LEVEL,
        PROTEIN_ARRAY_PROTEIN_LEVEL, //TODO - remove? I think this is not used anymore...
        PROTEIN_ARRAY_PHOSPHORYLATION; //TODO - remove? I think this is not used anymore...
    }

    private Integer geneticProfileId;
    private String stableId;
    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private GeneticAlterationType geneticAlterationType;
    private String datatype;
    private String name;
    private String description;
    private Boolean showProfileInAnalysisTab;
    private CancerStudy cancerStudy;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public GeneticAlterationType getGeneticAlterationType() {
        return geneticAlterationType;
    }

    public void setGeneticAlterationType(GeneticAlterationType geneticAlterationType) {
        this.geneticAlterationType = geneticAlterationType;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getShowProfileInAnalysisTab() {
        return showProfileInAnalysisTab;
    }

    public void setShowProfileInAnalysisTab(Boolean showProfileInAnalysisTab) {
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }
}
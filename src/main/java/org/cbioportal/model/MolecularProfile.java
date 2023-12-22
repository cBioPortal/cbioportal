package org.cbioportal.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class MolecularProfile implements Serializable {

    // Copied from org.mskcc.cbio.portal.model.GeneticAlterationType, if you alter this,
    // don't forget to change the original one too
    public enum MolecularAlterationType {
        MUTATION_EXTENDED,
        // uncalled mutations (mskcc internal) for showing read counts even if
        // mutation wasn't called
        MUTATION_UNCALLED,
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
        PROTEIN_ARRAY_PROTEIN_LEVEL,
        PROTEIN_ARRAY_PHOSPHORYLATION,
        GENESET_SCORE,
        GENERIC_ASSAY
    }
    
    public enum DataType {
        DISCRETE;
    }

    public enum ImportType {
        DISCRETE_LONG;
    }

    private Integer molecularProfileId;
    @NotNull
    private String stableId;
    private Integer cancerStudyId;
    @NotNull
    private String cancerStudyIdentifier;
    private MolecularAlterationType molecularAlterationType;
    private String genericAssayType;
    private String datatype;
    private String name;
    private String description;
    private Boolean showProfileInAnalysisTab;
    private CancerStudy cancerStudy;
    private Float pivotThreshold;
    private String sortOrder;
    @NotNull
    private Boolean patientLevel;

    public Integer getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(Integer molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
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

    public MolecularAlterationType getMolecularAlterationType() {
        return molecularAlterationType;
    }

    public void setMolecularAlterationType(MolecularAlterationType molecularAlterationType) {
        this.molecularAlterationType = molecularAlterationType;
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

    public Float getPivotThreshold() {
        return this.pivotThreshold;
    }

    public void setPivotThreshold(Float pivotThreshold) {
        this.pivotThreshold = pivotThreshold;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getGenericAssayType() {
        return genericAssayType;
    }

    public void setGenericAssayType(String genericAssayType) {
        this.genericAssayType = genericAssayType;
    }

    public Boolean getPatientLevel() {
        return patientLevel;
    }

    public void setPatientLevel(Boolean patientLevel) {
        this.patientLevel = patientLevel;
    }
}

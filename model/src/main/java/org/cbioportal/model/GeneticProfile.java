package org.cbioportal.model;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cbioportal.model.Sample.SampleType;

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
        PROTEIN_ARRAY_PROTEIN_LEVEL,
        PROTEIN_ARRAY_PHOSPHORYLATION, 
        GENESET_SCORE;
    }
    
    // Based on core/src/main/scripts/importer/cbioportal_common.py get_meta_file_type()
    // and core/src/main/scripts/importer/allowed_data_types.txt.
    // If you alter this, don't forget to update there too.
    public enum DataType {

        CONTINUOUS,
        LOG2_VALUE("LOG2-VALUE"),
        MAF,
        FUSION,
        Z_SCORE("Z-SCORE"),
        SV,
        //cna:
        SEG,
        DISCRETE,//(this one is also documented for expression data...but haven't seen any examples)
        //gistic / mutsig:
        Q_VALUE("Q-VALUE"),
        //gene set type:
        GSVA_SCORE("GSVA-SCORE"),
        P_VALUE("P-VALUE");

        private String value;

        DataType(String value) {
            this.value = value;
        }

        DataType() {
            this.value = this.name();
        }

        public String getValue() {
            return value;
        }

        public static DataType fromString(String value) {

            if (value != null) {
                for (DataType dataType : DataType.values()) {
                    if (value.equalsIgnoreCase(dataType.value)) {
                        return dataType;
                    }
                }
            }
            // if unrecognized type, throw error:
            throw new IllegalArgumentException("Unrecognized DATA_TYPE in DB: " + value + ". "
                    + "Should be one of: " + Stream.of(DataType.values()).map(DataType::getValue).collect(Collectors.joining(", ")));
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private Integer geneticProfileId;
    private String stableId;
    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private GeneticAlterationType geneticAlterationType;
    private DataType datatype;
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

    public DataType getDatatype() {
        return datatype;
    }

    public void setDatatype(DataType datatype) {
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
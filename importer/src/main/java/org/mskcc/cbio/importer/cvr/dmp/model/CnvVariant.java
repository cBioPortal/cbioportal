
package org.mskcc.cbio.importer.cvr.dmp.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "chromosome",
    "cnv_class_cv_id",
    "cnv_class_name",
    "cnv_filter_cv_id",
    "cnv_filter_name",
    "cnv_variant_id",
    "comments",
    "confidence_class",
    "confidence_cv_id",
    "cytoband",
    "gene_fold_change",
    "gene_id",
    "gene_p_value",
    "is_significant",
    "variant_status_cv_id",
    "variant_status_name"
})
public class CnvVariant {

    @JsonProperty("chromosome")
    private String chromosome;
    @JsonProperty("cnv_class_cv_id")
    private Integer cnvClassCvId;
    @JsonProperty("cnv_class_name")
    private String cnvClassName;
    @JsonProperty("cnv_filter_cv_id")
    private Integer cnvFilterCvId;
    @JsonProperty("cnv_filter_name")
    private String cnvFilterName;
    @JsonProperty("cnv_variant_id")
    private Integer cnvVariantId;
    @JsonProperty("comments")
    private Object comments;
    @JsonProperty("confidence_class")
    private String confidenceClass;
    @JsonProperty("confidence_cv_id")
    private Integer confidenceCvId;
    @JsonProperty("cytoband")
    private String cytoband;
    @JsonProperty("gene_fold_change")
    private Double geneFoldChange;
    @JsonProperty("gene_id")
    private String geneId;
    @JsonProperty("gene_p_value")
    private Double genePValue;
    @JsonProperty("is_significant")
    private Integer isSignificant;
    @JsonProperty("variant_status_cv_id")
    private Integer variantStatusCvId;
    @JsonProperty("variant_status_name")
    private String variantStatusName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonIgnore
    private Integer dmpSampleId;

    /**
     * 
     * @return
     *     The chromosome
     */
    @JsonProperty("chromosome")
    public String getChromosome() {
        return chromosome;
    }

    /**
     * 
     * @param chromosome
     *     The chromosome
     */
    @JsonProperty("chromosome")
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * 
     * @return
     *     The cnvClassCvId
     */
    @JsonProperty("cnv_class_cv_id")
    public Integer getCnvClassCvId() {
        return cnvClassCvId;
    }

    /**
     * 
     * @param cnvClassCvId
     *     The cnv_class_cv_id
     */
    @JsonProperty("cnv_class_cv_id")
    public void setCnvClassCvId(Integer cnvClassCvId) {
        this.cnvClassCvId = cnvClassCvId;
    }

    /**
     * 
     * @return
     *     The cnvClassName
     */
    @JsonProperty("cnv_class_name")
    public String getCnvClassName() {
        return cnvClassName;
    }

    /**
     * 
     * @param cnvClassName
     *     The cnv_class_name
     */
    @JsonProperty("cnv_class_name")
    public void setCnvClassName(String cnvClassName) {
        this.cnvClassName = cnvClassName;
    }

    /**
     * 
     * @return
     *     The cnvFilterCvId
     */
    @JsonProperty("cnv_filter_cv_id")
    public Integer getCnvFilterCvId() {
        return cnvFilterCvId;
    }

    /**
     * 
     * @param cnvFilterCvId
     *     The cnv_filter_cv_id
     */
    @JsonProperty("cnv_filter_cv_id")
    public void setCnvFilterCvId(Integer cnvFilterCvId) {
        this.cnvFilterCvId = cnvFilterCvId;
    }

    /**
     * 
     * @return
     *     The cnvFilterName
     */
    @JsonProperty("cnv_filter_name")
    public String getCnvFilterName() {
        return cnvFilterName;
    }

    /**
     * 
     * @param cnvFilterName
     *     The cnv_filter_name
     */
    @JsonProperty("cnv_filter_name")
    public void setCnvFilterName(String cnvFilterName) {
        this.cnvFilterName = cnvFilterName;
    }

    /**
     * 
     * @return
     *     The cnvVariantId
     */
    @JsonProperty("cnv_variant_id")
    public Integer getCnvVariantId() {
        return cnvVariantId;
    }

    /**
     * 
     * @param cnvVariantId
     *     The cnv_variant_id
     */
    @JsonProperty("cnv_variant_id")
    public void setCnvVariantId(Integer cnvVariantId) {
        this.cnvVariantId = cnvVariantId;
    }

    /**
     * 
     * @return
     *     The comments
     */
    @JsonProperty("comments")
    public Object getComments() {
        return comments;
    }

    /**
     * 
     * @param comments
     *     The comments
     */
    @JsonProperty("comments")
    public void setComments(Object comments) {
        this.comments = comments;
    }

    /**
     * 
     * @return
     *     The confidenceClass
     */
    @JsonProperty("confidence_class")
    public String getConfidenceClass() {
        return confidenceClass;
    }

    /**
     * 
     * @param confidenceClass
     *     The confidence_class
     */
    @JsonProperty("confidence_class")
    public void setConfidenceClass(String confidenceClass) {
        this.confidenceClass = confidenceClass;
    }

    /**
     * 
     * @return
     *     The confidenceCvId
     */
    @JsonProperty("confidence_cv_id")
    public Integer getConfidenceCvId() {
        return confidenceCvId;
    }

    /**
     * 
     * @param confidenceCvId
     *     The confidence_cv_id
     */
    @JsonProperty("confidence_cv_id")
    public void setConfidenceCvId(Integer confidenceCvId) {
        this.confidenceCvId = confidenceCvId;
    }

    /**
     * 
     * @return
     *     The cytoband
     */
    @JsonProperty("cytoband")
    public String getCytoband() {
        return cytoband;
    }

    /**
     * 
     * @param cytoband
     *     The cytoband
     */
    @JsonProperty("cytoband")
    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    /**
     * 
     * @return
     *     The geneFoldChange
     */
    @JsonProperty("gene_fold_change")
    public Double getGeneFoldChange() {
        return geneFoldChange;
    }

    /**
     * 
     * @param geneFoldChange
     *     The gene_fold_change
     */
    @JsonProperty("gene_fold_change")
    public void setGeneFoldChange(Double geneFoldChange) {
        this.geneFoldChange = geneFoldChange;
    }

    /**
     * 
     * @return
     *     The geneId
     */
    @JsonProperty("gene_id")
    public String getGeneId() {
        return geneId;
    }

    /**
     * 
     * @param geneId
     *     The gene_id
     */
    @JsonProperty("gene_id")
    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    /**
     * 
     * @return
     *     The genePValue
     */
    @JsonProperty("gene_p_value")
    public Double getGenePValue() {
        return genePValue;
    }

    /**
     * 
     * @param genePValue
     *     The gene_p_value
     */
    @JsonProperty("gene_p_value")
    public void setGenePValue(Double genePValue) {
        this.genePValue = genePValue;
    }

    /**
     * 
     * @return
     *     The isSignificant
     */
    @JsonProperty("is_significant")
    public Integer getIsSignificant() {
        return isSignificant;
    }

    /**
     * 
     * @param isSignificant
     *     The is_significant
     */
    @JsonProperty("is_significant")
    public void setIsSignificant(Integer isSignificant) {
        this.isSignificant = isSignificant;
    }

    /**
     * 
     * @return
     *     The variantStatusCvId
     */
    @JsonProperty("variant_status_cv_id")
    public Integer getVariantStatusCvId() {
        return variantStatusCvId;
    }

    /**
     * 
     * @param variantStatusCvId
     *     The variant_status_cv_id
     */
    @JsonProperty("variant_status_cv_id")
    public void setVariantStatusCvId(Integer variantStatusCvId) {
        this.variantStatusCvId = variantStatusCvId;
    }

    /**
     * 
     * @return
     *     The variantStatusName
     */
    @JsonProperty("variant_status_name")
    public String getVariantStatusName() {
        return variantStatusName;
    }

    /**
     * 
     * @param variantStatusName
     *     The variant_status_name
     */
    @JsonProperty("variant_status_name")
    public void setVariantStatusName(String variantStatusName) {
        this.variantStatusName = variantStatusName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(chromosome).append(cnvClassCvId).append(cnvClassName).append(cnvFilterCvId).append(cnvFilterName).append(cnvVariantId).append(comments).append(confidenceClass).append(confidenceCvId).append(cytoband).append(geneFoldChange).append(geneId).append(genePValue).append(isSignificant).append(variantStatusCvId).append(variantStatusName).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CnvVariant) == false) {
            return false;
        }
        CnvVariant rhs = ((CnvVariant) other);
        return new EqualsBuilder().append(chromosome, rhs.chromosome).append(cnvClassCvId, rhs.cnvClassCvId).append(cnvClassName, rhs.cnvClassName).append(cnvFilterCvId, rhs.cnvFilterCvId).append(cnvFilterName, rhs.cnvFilterName).append(cnvVariantId, rhs.cnvVariantId).append(comments, rhs.comments).append(confidenceClass, rhs.confidenceClass).append(confidenceCvId, rhs.confidenceCvId).append(cytoband, rhs.cytoband).append(geneFoldChange, rhs.geneFoldChange).append(geneId, rhs.geneId).append(genePValue, rhs.genePValue).append(isSignificant, rhs.isSignificant).append(variantStatusCvId, rhs.variantStatusCvId).append(variantStatusName, rhs.variantStatusName).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    public Integer getDmpSampleId() {
        return dmpSampleId;
    }

    public void setDmpSampleId(Integer dmpSampleId) {
        this.dmpSampleId = dmpSampleId;
    }
}

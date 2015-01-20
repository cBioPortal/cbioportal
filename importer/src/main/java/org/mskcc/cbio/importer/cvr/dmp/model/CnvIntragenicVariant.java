
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
    "cluster_1",
    "cluster_2",
    "cnv_variant_id",
    "comments",
    "confidence_cv_id",
    "cytoband",
    "gene_id",
    "refseq_acc",
    "variant_status_cv_id"
})
public class CnvIntragenicVariant {

    @JsonProperty("cluster_1")
    private String cluster1;
    @JsonProperty("cluster_2")
    private String cluster2;
    @JsonProperty("cnv_variant_id")
    private Integer cnvVariantId;
    @JsonProperty("comments")
    private Object comments;
    @JsonProperty("confidence_cv_id")
    private Integer confidenceCvId;
    @JsonProperty("cytoband")
    private String cytoband;
    @JsonProperty("gene_id")
    private String geneId;
    @JsonProperty("refseq_acc")
    private String refseqAcc;
    @JsonProperty("variant_status_cv_id")
    private Integer variantStatusCvId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The cluster1
     */
    @JsonProperty("cluster_1")
    public String getCluster1() {
        return cluster1;
    }

    /**
     * 
     * @param cluster1
     *     The cluster_1
     */
    @JsonProperty("cluster_1")
    public void setCluster1(String cluster1) {
        this.cluster1 = cluster1;
    }

    /**
     * 
     * @return
     *     The cluster2
     */
    @JsonProperty("cluster_2")
    public String getCluster2() {
        return cluster2;
    }

    /**
     * 
     * @param cluster2
     *     The cluster_2
     */
    @JsonProperty("cluster_2")
    public void setCluster2(String cluster2) {
        this.cluster2 = cluster2;
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
     *     The refseqAcc
     */
    @JsonProperty("refseq_acc")
    public String getRefseqAcc() {
        return refseqAcc;
    }

    /**
     * 
     * @param refseqAcc
     *     The refseq_acc
     */
    @JsonProperty("refseq_acc")
    public void setRefseqAcc(String refseqAcc) {
        this.refseqAcc = refseqAcc;
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
        return new HashCodeBuilder().append(cluster1).append(cluster2).append(cnvVariantId).append(comments).append(confidenceCvId).append(cytoband).append(geneId).append(refseqAcc).append(variantStatusCvId).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CnvIntragenicVariant) == false) {
            return false;
        }
        CnvIntragenicVariant rhs = ((CnvIntragenicVariant) other);
        return new EqualsBuilder().append(cluster1, rhs.cluster1).append(cluster2, rhs.cluster2).append(cnvVariantId, rhs.cnvVariantId).append(comments, rhs.comments).append(confidenceCvId, rhs.confidenceCvId).append(cytoband, rhs.cytoband).append(geneId, rhs.geneId).append(refseqAcc, rhs.refseqAcc).append(variantStatusCvId, rhs.variantStatusCvId).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}

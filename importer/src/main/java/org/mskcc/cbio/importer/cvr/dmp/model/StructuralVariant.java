package org.mskcc.cbio.importer.cvr.dmp.model;


import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/24/14.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
/*

    "num_cluster",
    "refseq_acc",

*/
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "comments",
        "confidence_class",
        "conn_type",
        "event_info",
        "site1_chrom",
        "site1_desc",
        "site1_gene",
        "site1_pos",
        "site2_chrom",
        "site2_desc",
        "site2_gene",
        "site2_pos",
        "sv_class_name",
        "sv_desc",
        "sv_length",
        "sv_variant_id",
        "variant_status_name"
})
public class StructuralVariant {


    @JsonProperty( "comments")
    private String comments;
    @JsonProperty("confidence_class")
    private String confidenceClass;
    @JsonProperty( "conn_type")
    private String connType;
    @JsonProperty("event_info")
    private String eventInfo;
    @JsonProperty("site1_chrom")
    private String site1Chrom;
    @JsonProperty("site1_desc")
    private String site1Desc;
    @JsonProperty("site1_gene")
    private String site1Gene;
    @JsonProperty("site1_pos")
    private Long site1Pos;
    @JsonProperty("site2_chrom")
    private String site2Chrom;
    @JsonProperty("site2_desc")
    private String site2Desc;
    @JsonProperty("site2_gene")
    private String site2Gene;
    @JsonProperty("site2_pos")
    private Long site2Pos;
    @JsonProperty("sv_class_name")
    private String svClassName;
    @JsonProperty("sv_desc")
    private String svDesc;
    @JsonProperty("sv_length")
    private Integer svLength;
    @JsonProperty("sv_variant_id")
    private Integer svVariantId;
    @JsonProperty("variant_status_name")
    private String variantStatusName;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonIgnore
    private String dmpSampleId;

    @JsonProperty( "comments")
    public String getComments() {
        return comments;
    }

    @JsonProperty( "comments")
    public void setComments(String comments) {
        this.comments = comments;
    }
    @JsonProperty("confidence_class")
    public String getConfidenceClass() {
        return confidenceClass;
    }
    @JsonProperty("confidence_class")
    public void setConfidenceClass(String confidenceClass) {
        this.confidenceClass = confidenceClass;
    }
    @JsonProperty( "conn_type")
    public String getConnType() {
        return connType;
    }
    @JsonProperty( "conn_type")
    public void setConnType(String connType) {
        this.connType = connType;
    }
    @JsonProperty("event_info")
    public String getEventInfo() {
        return eventInfo;
    }
    @JsonProperty("event_info")
    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }
    @JsonProperty("site1_chrom")
    public String getSite1Chrom() {
        return site1Chrom;
    }
    @JsonProperty("site1_chrom")
    public void setSite1Chrom(String site1Chrom) {
        this.site1Chrom = site1Chrom;
    }
    @JsonProperty("site1_desc")
    public String getSite1Desc() {
        return site1Desc;
    }
    @JsonProperty("site1_desc")
    public void setSite1Desc(String site1Desc) {
        this.site1Desc = site1Desc;
    }
    @JsonProperty("site1_gene")
    public String getSite1Gene() {
        return site1Gene;
    }
    @JsonProperty("site1_gene")
    public void setSite1Gene(String site1Gene) {
        this.site1Gene = site1Gene;
    }
    @JsonProperty("site1_pos")
    public Long getSite1Pos() {
        return site1Pos;
    }
    @JsonProperty("site1_pos")
    public void setSite1Pos(Long site1Pos) {
        this.site1Pos = site1Pos;
    }
    @JsonProperty("site2_chrom")
    public String getSite2Chrom() {
        return site2Chrom;
    }
    @JsonProperty("site2_chrom")
    public void setSite2Chrom(String site2Chrom) {
        this.site2Chrom = site2Chrom;
    }
    @JsonProperty("site2_desc")
    public String getSite2Desc() {
        return site2Desc;
    }
    @JsonProperty("site2_desc")
    public void setSite2Desc(String site2Desc) {
        this.site2Desc = site2Desc;
    }
    @JsonProperty("site2_gene")
    public String getSite2Gene() {
        return site2Gene;
    }
    @JsonProperty("site2_gene")
    public void setSite2Gene(String site2Gene) {
        this.site2Gene = site2Gene;
    }
    @JsonProperty("site2_pos")
    public Long getSite2Pos() {
        return site2Pos;
    }
    @JsonProperty("site2_pos")
    public void setSite2Pos(Long site2Pos) {
        this.site2Pos = site2Pos;
    }
    @JsonProperty("sv_class_name")
    public String getSvClassName() {
        return svClassName;
    }
    @JsonProperty("sv_class_name")
    public void setSvClassName(String svClassName) {
        this.svClassName = svClassName;
    }
    @JsonProperty("sv_desc")
    public String getSvDesc() {
        return svDesc;
    }
    @JsonProperty("sv_desc")
    public void setSvDesc(String svDesc) {
        this.svDesc = svDesc;
    }
    @JsonProperty("sv_length")
    public Integer getSvLength() {
        return svLength;
    }
    @JsonProperty("sv_length")
    public void setSvLength(Integer svLength) {
        this.svLength = svLength;
    }
    @JsonProperty("sv_variant_id")
    public Integer getSvVariantId() {
        return svVariantId;
    }
    @JsonProperty("sv_variant_id")
    public void setSvVariantId(Integer svVariantId) {
        this.svVariantId = svVariantId;
    }
    @JsonProperty("variant_status_name")
    public String getVariantStatusName() {
        return variantStatusName;
    }
    @JsonProperty("variant_status_name")
    public void setVariantStatusName(String variantStatusName) {
        this.variantStatusName = variantStatusName;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getDmpSampleId() {
        return dmpSampleId;
    }

    public void setDmpSampleId(String dmpSampleId) {
        this.dmpSampleId = dmpSampleId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    @Override
    public int hashCode(){
        return  new HashCodeBuilder().append(this.comments).append(this.confidenceClass)
                .append(this.dmpSampleId).append(this.eventInfo).append(this.site1Chrom)
                .append(this.site1Desc).append(this.site1Gene).append(this.site1Pos).append(this.site2Chrom)
                .append(this.site2Desc).append(this.site2Gene).append(this.site2Pos).append(this.svClassName)
                .append(this.getSvDesc()).append(this.svLength).append(this.svVariantId)
                .append(this.additionalProperties).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StructuralVariant) == false) {
            return false;
        }
        StructuralVariant rhs = ((StructuralVariant) other);
        return new EqualsBuilder()
                .append(this.comments,rhs.comments).append(this.confidenceClass,rhs.confidenceClass)
                .append(this.dmpSampleId,rhs.dmpSampleId).append(this.eventInfo,rhs.eventInfo)
                .append(this.site1Chrom,rhs.site1Chrom)
                .append(this.site1Desc,rhs.site1Desc)
                .append(this.site1Gene,rhs.site1Gene).append(this.site1Pos,rhs.site1Pos)
                .append(this.site2Chrom,rhs.site2Chrom)
                .append(this.site2Desc,rhs.site1Desc).append(this.site2Gene, rhs.site2Gene)
                .append(this.site2Pos,rhs.site2Pos).append(this.svClassName, rhs.svClassName)
                .append(this.svDesc,rhs.svDesc).append(this.svLength,rhs.svLength)
                .append(this.svVariantId, rhs.svVariantId)
                .append(this.additionalProperties,rhs.additionalProperties)
                .isEquals();
    }
}
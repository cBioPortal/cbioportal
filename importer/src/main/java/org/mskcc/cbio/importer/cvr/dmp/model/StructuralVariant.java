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
 * mod 03Feb2015 FJC - added support for new SV attributes
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "breakpoint_type",
        "comments",
        "confidence_class",
        "conn_type",
        "connection_type",
        "event_info",
        "mapq",
        "normal_read_count",
        "normal_variant_count",
        "paired_end_read_support",
        "site1_chrom",
        "site1_desc",
        "site1_gene",
        "site1_pos",
        "site2_chrom",
        "site2_desc",
        "site2_gene",
        "site2_pos",
        "split_read_support",
        "sv_class_name",
        "sv_desc",
        "sv_length",
        "sv_variant_id",
        "tumor_read_count",
        "tumor_variant_count",
        "variant_status_name"
})
public class StructuralVariant {

    @JsonProperty("breakpoint_type")
    private String breakpointType;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("confidence_class")
    private String confidenceClass;
    @JsonProperty("conn_type")
    private String connType;
    @JsonProperty("connection_type")
    private String connectionType;
    @JsonProperty("event_info")
    private String eventInfo;
    @JsonProperty("mapq")
    private Object mapq;
    @JsonProperty("normal_read_count")
    private Integer normalReadCount;
    @JsonProperty("normal_variant_count")
    private Integer normalVariantCount;
    @JsonProperty("paired_end_read_support")
    private Integer pairedEndReadSupport;
    @JsonProperty("site1_chrom")
    private String site1Chrom;
    @JsonProperty("site1_desc")
    private String site1Desc;
    @JsonProperty("site1_gene")
    private String site1Gene;
    @JsonProperty("site1_pos")
    private Integer site1Pos;
    @JsonProperty("site2_chrom")
    private String site2Chrom;
    @JsonProperty("site2_desc")
    private String site2Desc;
    @JsonProperty("site2_gene")
    private String site2Gene;
    @JsonProperty("site2_pos")
    private Integer site2Pos;
    @JsonProperty("split_read_support")
    private Integer splitReadSupport;
    @JsonProperty("sv_class_name")
    private String svClassName;
    @JsonProperty("sv_desc")
    private String svDesc;
    @JsonProperty("sv_length")
    private Integer svLength;
    @JsonProperty("sv_variant_id")
    private Integer svVariantId;
    @JsonProperty("tumor_read_count")
    private Integer tumorReadCount;
    @JsonProperty("tumor_variant_count")
    private Integer tumorVariantCount;
    @JsonProperty("variant_status_name")
    private String variantStatusName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonIgnore
    private String dmpSampleId;


    public void setDmpSampleId(String id) {
        this.dmpSampleId = id;
    }

    public String getDmpSampleId() {
        return this.dmpSampleId;
    }


    /**
     *
     * @return
     *     The breakpointType
     */
    @JsonProperty("breakpoint_type")
    public String getBreakpointType() {
        return breakpointType;
    }

    /**
     *
     * @param breakpointType
     *     The breakpoint_type
     */
    @JsonProperty("breakpoint_type")
    public void setBreakpointType(String breakpointType) {
        this.breakpointType = breakpointType;
    }

    /**
     *
     * @return
     *     The comments
     */
    @JsonProperty("comments")
    public String getComments() {
        return comments;
    }

    /**
     *
     * @param comments
     *     The comments
     */
    @JsonProperty("comments")
    public void setComments(String comments) {
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
     *     The connType
     */
    @JsonProperty("conn_type")
    public String getConnType() {
        return connType;
    }

    /**
     *
     * @param connType
     *     The conn_type
     */
    @JsonProperty("conn_type")
    public void setConnType(String connType) {
        this.connType = connType;
    }

    /**
     *
     * @return
     *     The connectionType
     */
    @JsonProperty("connection_type")
    public String getConnectionType() {
        return connectionType;
    }

    /**
     *
     * @param connectionType
     *     The connection_type
     */
    @JsonProperty("connection_type")
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     *
     * @return
     *     The eventInfo
     */
    @JsonProperty("event_info")
    public String getEventInfo() {
        return eventInfo;
    }

    /**
     *
     * @param eventInfo
     *     The event_info
     */
    @JsonProperty("event_info")
    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    /**
     *
     * @return
     *     The mapq
     */
    @JsonProperty("mapq")
    public Object getMapq() {
        return mapq;
    }

    /**
     *
     * @param mapq
     *     The mapq
     */
    @JsonProperty("mapq")
    public void setMapq(Object mapq) {
        this.mapq = mapq;
    }

    /**
     *
     * @return
     *     The normalReadCount
     */
    @JsonProperty("normal_read_count")
    public Integer getNormalReadCount() {
        return normalReadCount;
    }

    /**
     *
     * @param normalReadCount
     *     The normal_read_count
     */
    @JsonProperty("normal_read_count")
    public void setNormalReadCount(Integer normalReadCount) {
        this.normalReadCount = normalReadCount;
    }

    /**
     *
     * @return
     *     The normalVariantCount
     */
    @JsonProperty("normal_variant_count")
    public Integer getNormalVariantCount() {
        return normalVariantCount;
    }

    /**
     *
     * @param normalVariantCount
     *     The normal_variant_count
     */
    @JsonProperty("normal_variant_count")
    public void setNormalVariantCount(Integer normalVariantCount) {
        this.normalVariantCount = normalVariantCount;
    }

    /**
     *
     * @return
     *     The pairedEndReadSupport
     */
    @JsonProperty("paired_end_read_support")
    public Integer getPairedEndReadSupport() {
        return pairedEndReadSupport;
    }

    /**
     *
     * @param pairedEndReadSupport
     *     The paired_end_read_support
     */
    @JsonProperty("paired_end_read_support")
    public void setPairedEndReadSupport(Integer pairedEndReadSupport) {
        this.pairedEndReadSupport = pairedEndReadSupport;
    }

    /**
     *
     * @return
     *     The site1Chrom
     */
    @JsonProperty("site1_chrom")
    public String getSite1Chrom() {
        return site1Chrom;
    }

    /**
     *
     * @param site1Chrom
     *     The site1_chrom
     */
    @JsonProperty("site1_chrom")
    public void setSite1Chrom(String site1Chrom) {
        this.site1Chrom = site1Chrom;
    }

    /**
     *
     * @return
     *     The site1Desc
     */
    @JsonProperty("site1_desc")
    public String getSite1Desc() {
        return site1Desc;
    }

    /**
     *
     * @param site1Desc
     *     The site1_desc
     */
    @JsonProperty("site1_desc")
    public void setSite1Desc(String site1Desc) {
        this.site1Desc = site1Desc;
    }

    /**
     *
     * @return
     *     The site1Gene
     */
    @JsonProperty("site1_gene")
    public String getSite1Gene() {
        return site1Gene;
    }

    /**
     *
     * @param site1Gene
     *     The site1_gene
     */
    @JsonProperty("site1_gene")
    public void setSite1Gene(String site1Gene) {
        this.site1Gene = site1Gene;
    }

    /**
     *
     * @return
     *     The site1Pos
     */
    @JsonProperty("site1_pos")
    public Integer getSite1Pos() {
        return site1Pos;
    }

    /**
     *
     * @param site1Pos
     *     The site1_pos
     */
    @JsonProperty("site1_pos")
    public void setSite1Pos(Integer site1Pos) {
        this.site1Pos = site1Pos;
    }

    /**
     *
     * @return
     *     The site2Chrom
     */
    @JsonProperty("site2_chrom")
    public String getSite2Chrom() {
        return site2Chrom;
    }

    /**
     *
     * @param site2Chrom
     *     The site2_chrom
     */
    @JsonProperty("site2_chrom")
    public void setSite2Chrom(String site2Chrom) {
        this.site2Chrom = site2Chrom;
    }

    /**
     *
     * @return
     *     The site2Desc
     */
    @JsonProperty("site2_desc")
    public String getSite2Desc() {
        return site2Desc;
    }

    /**
     *
     * @param site2Desc
     *     The site2_desc
     */
    @JsonProperty("site2_desc")
    public void setSite2Desc(String site2Desc) {
        this.site2Desc = site2Desc;
    }

    /**
     *
     * @return
     *     The site2Gene
     */
    @JsonProperty("site2_gene")
    public String getSite2Gene() {
        return site2Gene;
    }

    /**
     *
     * @param site2Gene
     *     The site2_gene
     */
    @JsonProperty("site2_gene")
    public void setSite2Gene(String site2Gene) {
        this.site2Gene = site2Gene;
    }

    /**
     *
     * @return
     *     The site2Pos
     */
    @JsonProperty("site2_pos")
    public Integer getSite2Pos() {
        return site2Pos;
    }

    /**
     *
     * @param site2Pos
     *     The site2_pos
     */
    @JsonProperty("site2_pos")
    public void setSite2Pos(Integer site2Pos) {
        this.site2Pos = site2Pos;
    }

    /**
     *
     * @return
     *     The splitReadSupport
     */
    @JsonProperty("split_read_support")
    public Integer getSplitReadSupport() {
        return splitReadSupport;
    }

    /**
     *
     * @param splitReadSupport
     *     The split_read_support
     */
    @JsonProperty("split_read_support")
    public void setSplitReadSupport(Integer splitReadSupport) {
        this.splitReadSupport = splitReadSupport;
    }

    /**
     *
     * @return
     *     The svClassName
     */
    @JsonProperty("sv_class_name")
    public String getSvClassName() {
        return svClassName;
    }

    /**
     *
     * @param svClassName
     *     The sv_class_name
     */
    @JsonProperty("sv_class_name")
    public void setSvClassName(String svClassName) {
        this.svClassName = svClassName;
    }

    /**
     *
     * @return
     *     The svDesc
     */
    @JsonProperty("sv_desc")
    public String getSvDesc() {
        return svDesc;
    }

    /**
     *
     * @param svDesc
     *     The sv_desc
     */
    @JsonProperty("sv_desc")
    public void setSvDesc(String svDesc) {
        this.svDesc = svDesc;
    }

    /**
     *
     * @return
     *     The svLength
     */
    @JsonProperty("sv_length")
    public Integer getSvLength() {
        return svLength;
    }

    /**
     *
     * @param svLength
     *     The sv_length
     */
    @JsonProperty("sv_length")
    public void setSvLength(Integer svLength) {
        this.svLength = svLength;
    }

    /**
     *
     * @return
     *     The svVariantId
     */
    @JsonProperty("sv_variant_id")
    public Integer getSvVariantId() {
        return svVariantId;
    }

    /**
     *
     * @param svVariantId
     *     The sv_variant_id
     */
    @JsonProperty("sv_variant_id")
    public void setSvVariantId(Integer svVariantId) {
        this.svVariantId = svVariantId;
    }

    /**
     *
     * @return
     *     The tumorReadCount
     */
    @JsonProperty("tumor_read_count")
    public Integer getTumorReadCount() {
        return tumorReadCount;
    }

    /**
     *
     * @param tumorReadCount
     *     The tumor_read_count
     */
    @JsonProperty("tumor_read_count")
    public void setTumorReadCount(Integer tumorReadCount) {
        this.tumorReadCount = tumorReadCount;
    }

    /**
     *
     * @return
     *     The tumorVariantCount
     */
    @JsonProperty("tumor_variant_count")
    public Integer getTumorVariantCount() {
        return tumorVariantCount;
    }

    /**
     *
     * @param tumorVariantCount
     *     The tumor_variant_count
     */
    @JsonProperty("tumor_variant_count")
    public void setTumorVariantCount(Integer tumorVariantCount) {
        this.tumorVariantCount = tumorVariantCount;
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
        return new HashCodeBuilder().append(breakpointType).append(comments).append(confidenceClass).append(connType).append(connectionType).append(eventInfo).append(mapq).append(normalReadCount).append(normalVariantCount).append(pairedEndReadSupport).append(site1Chrom).append(site1Desc).append(site1Gene).append(site1Pos).append(site2Chrom).append(site2Desc).append(site2Gene).append(site2Pos).append(splitReadSupport).append(svClassName).append(svDesc).append(svLength).append(svVariantId).append(tumorReadCount).append(tumorVariantCount).append(variantStatusName).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StructuralVariant) == false) {
            return false;
        }
        StructuralVariant rhs = (StructuralVariant) other;
        return new EqualsBuilder().append(breakpointType, rhs.breakpointType).append(comments, rhs.comments).append(confidenceClass, rhs.confidenceClass).append(connType, rhs.connType).append(connectionType, rhs.connectionType).append(eventInfo, rhs.eventInfo).append(mapq, rhs.mapq).append(normalReadCount, rhs.normalReadCount).append(normalVariantCount, rhs.normalVariantCount).append(pairedEndReadSupport, rhs.pairedEndReadSupport).append(site1Chrom, rhs.site1Chrom).append(site1Desc, rhs.site1Desc).append(site1Gene, rhs.site1Gene).append(site1Pos, rhs.site1Pos).append(site2Chrom, rhs.site2Chrom).append(site2Desc, rhs.site2Desc).append(site2Gene, rhs.site2Gene).append(site2Pos, rhs.site2Pos).append(splitReadSupport, rhs.splitReadSupport).append(svClassName, rhs.svClassName).append(svDesc, rhs.svDesc).append(svLength, rhs.svLength).append(svVariantId, rhs.svVariantId).append(tumorReadCount, rhs.tumorReadCount).append(tumorVariantCount, rhs.tumorVariantCount).append(variantStatusName, rhs.variantStatusName).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}

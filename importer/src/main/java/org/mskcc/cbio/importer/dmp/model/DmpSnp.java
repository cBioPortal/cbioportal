/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.dmp.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;


public interface DmpSnp {
    
     @JsonAnySetter
    public void setDmpSampleId(Integer id);

    /**
     *
     * @return
     *     The aaChange
     */
    @JsonProperty(value = "aa_change")
    String getAaChange();

    @JsonAnyGetter
    Map<String, Object> getAdditionalProperties();

    /**
     *
     * @return
     *     The altAllele
     */
    @JsonProperty(value = "alt_allele")
    String getAltAllele();

    /**
     *
     * @return
     *     The cDNAChange
     */
    @JsonProperty(value = "cDNA_change")
    String getCDNAChange();

    /**
     *
     * @return
     *     The chromosome
     */
    @JsonProperty(value = "chromosome")
    String getChromosome();

    /**
     *
     * @return
     *     The comments
     */
    @JsonProperty(value = "comments")
    Object getComments();

    /**
     *
     * @return
     *     The confidenceClass
     */
    @JsonProperty(value = "confidence_class")
    String getConfidenceClass();

    /**
     *
     * @return
     *     The confidenceCvId
     */
    @JsonProperty(value = "confidence_cv_id")
    Integer getConfidenceCvId();

    /**
     *
     * @return
     *     The cosmicId
     */
    @JsonProperty(value = "cosmic_id")
    String getCosmicId();

    /**
     *
     * @return
     *     The dbSNPId
     */
    @JsonProperty(value = "dbSNP_id")
    String getDbSNPId();

    @JsonAnyGetter
    Integer getDmpSampleId();

    /**
     *
     * @return
     *     The dmpSampleMrevId
     */
    @JsonProperty(value = "dmp_sample_mrev_id")
    Integer getDmpSampleMrevId();

    /**
     *
     * @return
     *     The dmpSampleSoId
     */
    @JsonProperty(value = "dmp_sample_so_id")
    Integer getDmpSampleSoId();

    /**
     *
     * @return
     *     The dmpVariantId
     */
    @JsonProperty(value = "dmp_variant_id")
    Integer getDmpVariantId();

    /**
     *
     * @return
     *     The exonNum
     */
    @JsonProperty(value = "exon_num")
    String getExonNum();

    /**
     *
     * @return
     *     The geneId
     */
    @JsonProperty(value = "gene_id")
    String getGeneId();

    /**
     *
     * @return
     *     The isHotspot
     */
    @JsonProperty(value = "is_hotspot")
    Integer getIsHotspot();

    /**
     *
     * @return
     *     The mafreq1000g
     */
    @JsonProperty(value = "mafreq_1000g")
    String getMafreq1000g();

    /**
     *
     * @return
     *     The mrevComments
     */
    @JsonProperty(value = "mrev_comments")
    String getMrevComments();

    /**
     *
     * @return
     *     The mrevStatusCvId
     */
    @JsonProperty(value = "mrev_status_cv_id")
    Integer getMrevStatusCvId();

    /**
     *
     * @return
     *     The mrevStatusName
     */
    @JsonProperty(value = "mrev_status_name")
    String getMrevStatusName();

    /**
     *
     * @return
     *     The normalAd
     */
    @JsonProperty(value = "normal_ad")
    Integer getNormalAd();

    /**
     *
     * @return
     *     The normalDp
     */
    @JsonProperty(value = "normal_dp")
    Integer getNormalDp();

    /**
     *
     * @return
     *     The normalVfreq
     */
    @JsonProperty(value = "normal_vfreq")
    Double getNormalVfreq();

    /**
     *
     * @return
     *     The occuranceInNormal
     */
    @JsonProperty(value = "occurance_in_normal")
    String getOccuranceInNormal();

    /**
     *
     * @return
     *     The occuranceInPop
     */
    @JsonProperty(value = "occurance_in_pop")
    Object getOccuranceInPop();

    /**
     *
     * @return
     *     The refAllele
     */
    @JsonProperty(value = "ref_allele")
    String getRefAllele();

    /**
     *
     * @return
     *     The snpIndelVariantId
     */
    @JsonProperty(value = "snp_indel_variant_id")
    Integer getSnpIndelVariantId();

    /**
     *
     * @return
     *     The soComments
     */
    @JsonProperty(value = "so_comments")
    String getSoComments();

    /**
     *
     * @return
     *     The soStatusCvId
     */
    @JsonProperty(value = "so_status_cv_id")
    Integer getSoStatusCvId();

    /**
     *
     * @return
     *     The soStatusName
     */
    @JsonProperty(value = "so_status_name")
    String getSoStatusName();

    /**
     *
     * @return
     *     The startPosition
     */
    @JsonProperty(value = "start_position")
    Integer getStartPosition();

    /**
     *
     * @return
     *     The transcriptId
     */
    @JsonProperty(value = "transcript_id")
    String getTranscriptId();

    /**
     *
     * @return
     *     The tumorAd
     */
    @JsonProperty(value = "tumor_ad")
    Integer getTumorAd();

    /**
     *
     * @return
     *     The tumorDp
     */
    @JsonProperty(value = "tumor_dp")
    Integer getTumorDp();

    /**
     *
     * @return
     *     The tumorVfreq
     */
    @JsonProperty(value = "tumor_vfreq")
    Double getTumorVfreq();

    /**
     *
     * @return
     *     The variantClass
     */
    @JsonProperty(value = "variant_class")
    String getVariantClass();

    /**
     *
     * @return
     *     The variantClassCvId
     */
    @JsonProperty(value = "variant_class_cv_id")
    Integer getVariantClassCvId();

    /**
     *
     * @return
     *     The variantStatusCvId
     */
    @JsonProperty(value = "variant_status_cv_id")
    Integer getVariantStatusCvId();

    /**
     *
     * @return
     *     The variantStatusName
     */
    @JsonProperty(value = "variant_status_name")
    String getVariantStatusName();
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;

/**
 *
 * @author jake
 */
public class SVMixin {
    private String sampleId;
    private String annotation;
    private String breakpoint_type;
    private String comments;
    private String confidence_class;
    private String conn_type;
    private String connection_type;
    private String event_info;
    private Integer mapq;
    private Integer normal_read_count;
    private Integer normal_variant_count;
    private Integer paired_end_read_support;
    private String site1_chrom;
    private String site1_desc;
    private String site1_gene;
    private Integer site1_pos;
    private String site2_chrom;
    private String site2_desc;
    private String site2_gene;
    private Integer site2_pos;
    private Integer split_read_support;
    private String sv_class_name;
    private String sv_desc;
    private Integer sv_length;
    private Integer sv_variant_id;
    private Integer tumor_read_count;
    private Integer tumor_variant_count;
    private String variant_status_name;
    private String geneticProfileId;
    
    @JsonUnwrapped
    private Gene gene1;
    
    @JsonUnwrapped
    private Gene gene2;
    
    @JsonUnwrapped
    private Sample sample;
    
    @JsonUnwrapped
    private GeneticProfile geneticProfile;
    
}

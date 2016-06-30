
package org.cbioportal.model;

import java.io.Serializable;

public class SV implements Serializable{
    
    private String sampleId;
    private String annotation;
    private String breakpoint_type;
    private String comments;
    private String confidence_class;
    private String conn_type;
    private String connection_type;
    private String event_info;
    private String mapq;
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
    private Integer geneticProfileId;
    private GeneticProfile geneticProfile;
    private Gene gene1;
    private Gene gene2;
    private Sample sample;
    
    public GeneticProfile getGeneticProfile(){
        return geneticProfile;
    }
    
    public void setGeneticProfile(GeneticProfile geneticProfile){
        this.geneticProfile = geneticProfile;
    }
    
    public Sample getSample(){
        return sample;
    }
    
    public void setSample(Sample sample){
        this.sample = sample;
    }

    public Gene getGene1() {
        return gene1;
    }

    public void setGene1(Gene gene1) {
        this.gene1 = gene1;
    }

    public Gene getGene2() {
        return gene2;
    }

    public void setGene2(Gene gene2) {
        this.gene2 = gene2;
    }
    
    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getBreakpoint_type() {
        return breakpoint_type;
    }

    public void setBreakpoint_type(String breakpoint_type) {
        this.breakpoint_type = breakpoint_type;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getConfidence_class() {
        return confidence_class;
    }

    public void setConfidence_class(String confidence_class) {
        this.confidence_class = confidence_class;
    }

    public String getConn_type() {
        return conn_type;
    }

    public void setConn_type(String conn_type) {
        this.conn_type = conn_type;
    }

    public String getConnection_type() {
        return connection_type;
    }

    public void setConnection_type(String connection_type) {
        this.connection_type = connection_type;
    }

    public String getEvent_info() {
        return event_info;
    }

    public void setEvent_info(String event_info) {
        this.event_info = event_info;
    }

    public String getMapq() {
        return mapq;
    }

    public void setMapq(String mapq) {
        this.mapq = mapq;
    }

    public Integer getNormal_read_count() {
        return normal_read_count;
    }

    public void setNormal_read_count(Integer normal_read_count) {
        this.normal_read_count = normal_read_count;
    }

    public Integer getNormal_variant_count() {
        return normal_variant_count;
    }

    public void setNormal_variant_count(Integer normal_variant_count) {
        this.normal_variant_count = normal_variant_count;
    }

    public Integer getPaired_end_read_support() {
        return paired_end_read_support;
    }

    public void setPaired_end_read_support(Integer paired_end_read_support) {
        this.paired_end_read_support = paired_end_read_support;
    }

    public String getSite1_chrom() {
        return site1_chrom;
    }

    public void setSite1_chrom(String site1_chrom) {
        this.site1_chrom = site1_chrom;
    }

    public String getSite1_desc() {
        return site1_desc;
    }

    public void setSite1_desc(String site1_desc) {
        this.site1_desc = site1_desc;
    }

    public String getSite1_gene() {
        return site1_gene;
    }

    public void setSite1_gene(String site1_gene) {
        this.site1_gene = site1_gene;
    }

    public Integer getSite1_pos() {
        return site1_pos;
    }

    public void setSite1_pos(Integer site1_pos) {
        this.site1_pos = site1_pos;
    }

    public String getSite2_chrom() {
        return site2_chrom;
    }

    public void setSite2_chrom(String site2_chrom) {
        this.site2_chrom = site2_chrom;
    }

    public String getSite2_desc() {
        return site2_desc;
    }

    public void setSite2_desc(String site2_desc) {
        this.site2_desc = site2_desc;
    }

    public String getSite2_gene() {
        return site2_gene;
    }

    public void setSite2_gene(String site2_gene) {
        this.site2_gene = site2_gene;
    }

    public Integer getSite2_pos() {
        return site2_pos;
    }

    public void setSite2_pos(Integer site2_pos) {
        this.site2_pos = site2_pos;
    }

    public Integer getSplit_read_support() {
        return split_read_support;
    }

    public void setSplit_read_support(Integer split_read_support) {
        this.split_read_support = split_read_support;
    }

    public String getSv_class_name() {
        return sv_class_name;
    }

    public void setSv_class_name(String sv_class_name) {
        this.sv_class_name = sv_class_name;
    }

    public String getSv_desc() {
        return sv_desc;
    }

    public void setSv_desc(String sv_desc) {
        this.sv_desc = sv_desc;
    }

    public Integer getSv_length() {
        return sv_length;
    }

    public void setSv_length(Integer sv_length) {
        this.sv_length = sv_length;
    }

    public Integer getSv_variant_id() {
        return sv_variant_id;
    }

    public void setSv_variant_id(Integer sv_variant_id) {
        this.sv_variant_id = sv_variant_id;
    }

    public Integer getTumor_read_count() {
        return tumor_read_count;
    }

    public void setTumor_read_count(Integer tumor_read_count) {
        this.tumor_read_count = tumor_read_count;
    }

    public Integer getTumor_variant_count() {
        return tumor_variant_count;
    }

    public void setTumor_variant_count(Integer tumor_variant_count) {
        this.tumor_variant_count = tumor_variant_count;
    }

    public String getVariant_status_name() {
        return variant_status_name;
    }

    public void setVariant_status_name(String variant_status_name) {
        this.variant_status_name = variant_status_name;
    }
    
    public Integer getGeneticProfileId(){
        return geneticProfileId;
    }
    
    public void setGeneticProfileId(Integer geneticProfileId){
        this.geneticProfileId = geneticProfileId;
    }
    

    
   
    
    
}

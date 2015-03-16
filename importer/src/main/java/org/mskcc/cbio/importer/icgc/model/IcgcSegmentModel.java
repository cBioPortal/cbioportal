package org.mskcc.cbio.importer.icgc.model;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentModel;

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
 * Created by criscuof on 12/19/14.
 */
public class IcgcSegmentModel  extends SegmentModel{

    private String cgc_donor_id;
    private String project_code;
    private String icgc_specimen_id;
    private String icgc_sample_id;
    private String matched_icgc_sample_id;
    private String submitted_sample_id;
    private String submitted_matched_sample_id;
    private String mutation_type;
    private String copy_number;
    private String segment_mean;
    private String segment_median;
    private String chromosome;
    private String chromosome_start;
    private String chromosome_end;
    private String assembly_version;
    private String chromosome_start_range;
    private String chromosome_end_range;
    private String start_probe_id;
    private String end_probe_id;
    private String sequencing_strategy;
    private String quality_score;
    private String probability;
    private String is_annotated;
    private String verification_status;
    private String verification_platform;
    private String gene_affected;
    private String transcript_affected;
    private String gene_build_version;
    private String platform;
    private String experimental_protocol;
    private String base_calling_algorithm;
    private String alignment_algorithm;
    private String variation_calling_algorithm;
    private String other_analysis_algorithm;
    private String seq_coverage;
    private String raw_data_repository;
    private String raw_data_accession;


    public String getCgc_donor_id() {
        return cgc_donor_id;
    }

    public void setCgc_donor_id(String cgc_donor_id) {
        this.cgc_donor_id = cgc_donor_id;
    }

    public String getProject_code() {
        return project_code;
    }

    public void setProject_code(String project_code) {
        this.project_code = project_code;
    }

    public String getIcgc_specimen_id() {
        return icgc_specimen_id;
    }

    public void setIcgc_specimen_id(String icgc_specimen_id) {
        this.icgc_specimen_id = icgc_specimen_id;
    }

    public String getIcgc_sample_id() {
        return icgc_sample_id;
    }

    public void setIcgc_sample_id(String icgc_sample_id) {
        this.icgc_sample_id = icgc_sample_id;
    }

    public String getMatched_icgc_sample_id() {
        return matched_icgc_sample_id;
    }

    public void setMatched_icgc_sample_id(String matched_icgc_sample_id) {
        this.matched_icgc_sample_id = matched_icgc_sample_id;
    }

    public String getSubmitted_sample_id() {
        return submitted_sample_id;
    }

    public void setSubmitted_sample_id(String submitted_sample_id) {
        this.submitted_sample_id = submitted_sample_id;
    }

    public String getSubmitted_matched_sample_id() {
        return submitted_matched_sample_id;
    }

    public void setSubmitted_matched_sample_id(String submitted_matched_sample_id) {
        this.submitted_matched_sample_id = submitted_matched_sample_id;
    }

    public String getMutation_type() {
        return mutation_type;
    }

    public void setMutation_type(String mutation_type) {
        this.mutation_type = mutation_type;
    }

    public String getCopy_number() {
        return copy_number;
    }

    public void setCopy_number(String copy_number) {
        this.copy_number = copy_number;
    }

    /*
    ICGC studies are inconsistent about using the segment_mean & segment_median columns to
    persist variation data. Return whichever column is used
     */
    public String getSegment_mean() {
        return this.segment_mean;
    }

    public void setSegment_mean(String segment_mean) {
        this.segment_mean = segment_mean;
    }

    public String getSegment_median() {
        return segment_median;
    }

    public void setSegment_median(String segment_median) {
        this.segment_median = segment_median;
    }


    @Override
    public String getID() {
        return this.getIcgc_sample_id();
    }

    public String getChromosome() {
        return this.chromosome;
    }

    @Override
    public String getLocStart() {
        return this.getChromosome_start();
    }

    @Override
    public String getLocEnd() {
        return this.getChromosome_end();
    }

    @Override
    public String getNumMark() {
        return "";
    }
    /*
       ICGC studies are inconsistent about using the segment_mean & segment_median columns to
       persist variation data. Return whichever column is used
        */
    @Override
    public String getSegMean() {
        return (!Strings.isNullOrEmpty(this.segment_mean))
                ?this.segment_mean
                :this.segment_median;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getChromosome_start() {
        return chromosome_start;
    }

    public void setChromosome_start(String chromosome_start) {
        this.chromosome_start = chromosome_start;
    }

    public String getChromosome_end() {
        return chromosome_end;
    }

    public void setChromosome_end(String chromosome_end) {
        this.chromosome_end = chromosome_end;
    }

    public String getAssembly_version() {
        return assembly_version;
    }

    public void setAssembly_version(String assembly_version) {
        this.assembly_version = assembly_version;
    }

    public String getChromosome_start_range() {
        return chromosome_start_range;
    }

    public void setChromosome_start_range(String chromosome_start_range) {
        this.chromosome_start_range = chromosome_start_range;
    }

    public String getChromosome_end_range() {
        return chromosome_end_range;
    }

    public void setChromosome_end_range(String chromosome_end_range) {
        this.chromosome_end_range = chromosome_end_range;
    }

    public String getStart_probe_id() {
        return start_probe_id;
    }

    public void setStart_probe_id(String start_probe_id) {
        this.start_probe_id = start_probe_id;
    }

    public String getEnd_probe_id() {
        return end_probe_id;
    }

    public void setEnd_probe_id(String end_probe_id) {
        this.end_probe_id = end_probe_id;
    }

    public String getSequencing_strategy() {
        return sequencing_strategy;
    }

    public void setSequencing_strategy(String sequencing_strategy) {
        this.sequencing_strategy = sequencing_strategy;
    }

    public String getQuality_score() {
        return quality_score;
    }

    public void setQuality_score(String quality_score) {
        this.quality_score = quality_score;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getIs_annotated() {
        return is_annotated;
    }

    public void setIs_annotated(String is_annotated) {
        this.is_annotated = is_annotated;
    }

    public String getVerification_status() {
        return verification_status;
    }

    public void setVerification_status(String verification_status) {
        this.verification_status = verification_status;
    }

    public String getVerification_platform() {
        return verification_platform;
    }

    public void setVerification_platform(String verification_platform) {
        this.verification_platform = verification_platform;
    }

    public String getGene_affected() {
        return gene_affected;
    }

    public void setGene_affected(String gene_affected) {
        this.gene_affected = gene_affected;
    }

    public String getTranscript_affected() {
        return transcript_affected;
    }

    public void setTranscript_affected(String transcript_affected) {
        this.transcript_affected = transcript_affected;
    }

    public String getGene_build_version() {
        return gene_build_version;
    }

    public void setGene_build_version(String gene_build_version) {
        this.gene_build_version = gene_build_version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getExperimental_protocol() {
        return experimental_protocol;
    }

    public void setExperimental_protocol(String experimental_protocol) {
        this.experimental_protocol = experimental_protocol;
    }

    public String getBase_calling_algorithm() {
        return base_calling_algorithm;
    }

    public void setBase_calling_algorithm(String base_calling_algorithm) {
        this.base_calling_algorithm = base_calling_algorithm;
    }

    public String getAlignment_algorithm() {
        return alignment_algorithm;
    }

    public void setAlignment_algorithm(String alignment_algorithm) {
        this.alignment_algorithm = alignment_algorithm;
    }

    public String getVariation_calling_algorithm() {
        return variation_calling_algorithm;
    }

    public void setVariation_calling_algorithm(String variation_calling_algorithm) {
        this.variation_calling_algorithm = variation_calling_algorithm;
    }

    public String getOther_analysis_algorithm() {
        return other_analysis_algorithm;
    }

    public void setOther_analysis_algorithm(String other_analysis_algorithm) {
        this.other_analysis_algorithm = other_analysis_algorithm;
    }

    public String getSeq_coverage() {
        return seq_coverage;
    }

    public void setSeq_coverage(String seq_coverage) {
        this.seq_coverage = seq_coverage;
    }

    public String getRaw_data_repository() {
        return raw_data_repository;
    }

    public void setRaw_data_repository(String raw_data_repository) {
        this.raw_data_repository = raw_data_repository;
    }

    public String getRaw_data_accession() {
        return raw_data_accession;
    }

    public void setRaw_data_accession(String raw_data_accession) {
        this.raw_data_accession = raw_data_accession;
    }
}

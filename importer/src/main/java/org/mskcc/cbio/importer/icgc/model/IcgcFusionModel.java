package org.mskcc.cbio.importer.icgc.model;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.mskcc.cbio.importer.foundation.support.FoundationCommonNames;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.icgc.support.IcgcUtil;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.util.GeneSymbolIDMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

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
 * Created by criscuof on 12/27/14.
 */
public class IcgcFusionModel extends FusionModel {



    private String icgc_donor_id;
    private String project_code;
    private String icgc_specimen_id;
    private String icgc_sample_id;
    private String submitted_sample_id;
    private String submitted_matched_sample_id;
    private String variant_type;
    private String sv_id;
    private String placement;
    private String annotation;
    private String interpreted_annotation;
    private String chr_from;
    private String chr_from_bkpt;
    private String chr_from_strand;
    private String chr_from_range;
    private String chr_from_flanking_seq;
    private String chr_to;
    private String chr_to_bkpt;
    private String chr_to_strand;
    private String chr_to_range;
    private String chr_to_flanking_seq;
    private String assembly_version;
    private String sequencing_strategy;
    private String microhomology_sequence;
    private String non_templated_sequence;
    private String evidence;
    private String quality_score;
    private String probability;
    private String zygosity;
    private String verification_status;
    private String verification_platform;
    private String gene_affected_by_bkpt_from;
    private String gene_affected_by_bkpt_to;
    private String transcript_affected_by_bkpt_from;
    private String transcript_affected_by_bkpt_to;
    private String bkpt_from_context;
    private String bkpt_to_context;
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

    public static Function<IcgcFusionModel, String> getTransformationFunction() {
        return transformationFunction;
    }

    @Override
    public String getGene() {
        // the gene affected column is usually empty
        if(!Strings.isNullOrEmpty(this.getGene_affected_by_bkpt_from())){
            return this.getGene_affected_by_bkpt_from();
        }
        // try to resolve name by position
        String geneName = geneMapper.findGeneNameByGenomicPosition(this.getChr_from(), this.getChr_from_bkpt(),
                this.chr_from_strand);
        return (!Strings.isNullOrEmpty(geneName))? geneName : StagingCommonNames.INTERGENIC;

    }

    @Override
    public String getEntrezGeneId() {
        return IcgcFunctionLibrary.resolveEntrezIdFromGeneName.apply(this.getGene());
    }

    @Override
    public String getCenter() {
        return this.getProject_code();
}

    @Override
    public String getTumorSampleBarcode() {
        return this.getIcgc_sample_id();
    }

    @Override
    public String getFusion() {
       StringBuffer sb = new StringBuffer();
        if(!Strings.isNullOrEmpty(this.getGene())){
            sb.append(this.getGene() +"-");
        }
        String geneTo = geneMapper.findGeneNameByGenomicPosition(this.getChr_to(), this.getChr_to_bkpt(),
                this.chr_to_strand);

        if(!Strings.isNullOrEmpty(geneTo)){
            sb.append(geneTo +"-");
        }
        sb.append(this.getAnnotation());

        return sb.toString();
    }

    @Override
    public String getDNASupport() {
        return this.getEvidence();
    }

    @Override
    public String getRNASupport() {
        return  FoundationCommonNames.DEFAULT_RNA_SUPPORT;
    }

    @Override
    public String getMethod() {
        return this.getExperimental_protocol();
    }

    @Override
    public String getFrame() {
        return this.getVariant_type();
    }

    public static String[] getFieldNames() {
        return StagingUtils.resolveFieldNames(IcgcFusionModel.class);
    }

    private final static Function<IcgcFusionModel, String> transformationFunction = new Function<IcgcFusionModel, String>() {
        @Override
        public String apply(final IcgcFusionModel mm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, mm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };

    public String getIcgc_donor_id() {
        return icgc_donor_id;
    }

    public void setIcgc_donor_id(String icgc_donor_id) {
        this.icgc_donor_id = icgc_donor_id;
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

    public String getVariant_type() {
        return variant_type;
    }

    public void setVariant_type(String variant_type) {
        this.variant_type = variant_type;
    }

    public String getSv_id() {
        return sv_id;
    }

    public void setSv_id(String sv_id) {
        this.sv_id = sv_id;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getInterpreted_annotation() {
        return interpreted_annotation;
    }

    public void setInterpreted_annotation(String interpreted_annotation) {
        this.interpreted_annotation = interpreted_annotation;
    }

    public String getChr_from() {
        return chr_from;
    }

    public void setChr_from(String chr_from) {
        this.chr_from = chr_from;
    }

    public String getChr_from_bkpt() {
        return chr_from_bkpt;
    }

    public void setChr_from_bkpt(String chr_from_bkpt) {
        this.chr_from_bkpt = chr_from_bkpt;
    }

    public String getChr_from_strand() {
        return chr_from_strand;
    }

    public void setChr_from_strand(String chr_from_strand) {
        this.chr_from_strand = chr_from_strand;
    }

    public String getChr_from_range() {
        return chr_from_range;
    }

    public void setChr_from_range(String chr_from_range) {
        this.chr_from_range = chr_from_range;
    }

    public String getChr_from_flanking_seq() {
        return chr_from_flanking_seq;
    }

    public void setChr_from_flanking_seq(String chr_from_flanking_seq) {
        this.chr_from_flanking_seq = chr_from_flanking_seq;
    }

    public String getChr_to() {
        return chr_to;
    }

    public void setChr_to(String chr_to) {
        this.chr_to = chr_to;
    }

    public String getChr_to_bkpt() {
        return chr_to_bkpt;
    }

    public void setChr_to_bkpt(String chr_to_bkpt) {
        this.chr_to_bkpt = chr_to_bkpt;
    }

    public String getChr_to_strand() {
        return chr_to_strand;
    }

    public void setChr_to_strand(String chr_to_strand) {
        this.chr_to_strand = chr_to_strand;
    }

    public String getChr_to_range() {
        return chr_to_range;
    }

    public void setChr_to_range(String chr_to_range) {
        this.chr_to_range = chr_to_range;
    }

    public String getChr_to_flanking_seq() {
        return chr_to_flanking_seq;
    }

    public void setChr_to_flanking_seq(String chr_to_flanking_seq) {
        this.chr_to_flanking_seq = chr_to_flanking_seq;
    }

    public String getAssembly_version() {
        return assembly_version;
    }

    public void setAssembly_version(String assembly_version) {
        this.assembly_version = assembly_version;
    }

    public String getSequencing_strategy() {
        return sequencing_strategy;
    }

    public void setSequencing_strategy(String sequencing_strategy) {
        this.sequencing_strategy = sequencing_strategy;
    }

    public String getMicrohomology_sequence() {
        return microhomology_sequence;
    }

    public void setMicrohomology_sequence(String microhomology_sequence) {
        this.microhomology_sequence = microhomology_sequence;
    }

    public String getNon_templated_sequence() {
        return non_templated_sequence;
    }

    public void setNon_templated_sequence(String non_templated_sequence) {
        this.non_templated_sequence = non_templated_sequence;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
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

    public String getZygosity() {
        return zygosity;
    }

    public void setZygosity(String zygosity) {
        this.zygosity = zygosity;
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

    public String getGene_affected_by_bkpt_from() {
        if(!Strings.isNullOrEmpty(this.getGene_affected_by_bkpt_from())) {
            return gene_affected_by_bkpt_from;
        }
        return geneMapper.findGeneNameByGenomicPosition(this.chr_from,
                this.getChr_from_bkpt(), this.chr_from_strand);
    }

    public void setGene_affected_by_bkpt_from(String gene_affected_by_bkpt_from) {
        this.gene_affected_by_bkpt_from = gene_affected_by_bkpt_from;
    }

    public String getGene_affected_by_bkpt_to() {

        if (!Strings.isNullOrEmpty( gene_affected_by_bkpt_to)) {
            return gene_affected_by_bkpt_to;
        }
        return geneMapper.findGeneNameByGenomicPosition(this.chr_to, this.chr_to_bkpt,this.chr_to_strand);
    }

    public void setGene_affected_by_bkpt_to(String gene_affected_by_bkpt_to) {
        this.gene_affected_by_bkpt_to = gene_affected_by_bkpt_to;
    }

    public String getTranscript_affected_by_bkpt_from() {
        return transcript_affected_by_bkpt_from;
    }

    public void setTranscript_affected_by_bkpt_from(String transcript_affected_by_bkpt_from) {
        this.transcript_affected_by_bkpt_from = transcript_affected_by_bkpt_from;
    }

    public String getTranscript_affected_by_bkpt_to() {
        return transcript_affected_by_bkpt_to;
    }

    public void setTranscript_affected_by_bkpt_to(String transcript_affected_by_bkpt_to) {
        this.transcript_affected_by_bkpt_to = transcript_affected_by_bkpt_to;
    }

    public String getBkpt_from_context() {
        return bkpt_from_context;
    }

    public void setBkpt_from_context(String bkpt_from_context) {
        this.bkpt_from_context = bkpt_from_context;
    }

    public String getBkpt_to_context() {
        return bkpt_to_context;
    }

    public void setBkpt_to_context(String bkpt_to_context) {
        this.bkpt_to_context = bkpt_to_context;
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

    public void swapToAndFromLocations() {
        String affectedFrom = this.getGene_affected_by_bkpt_from();
        String chrFrom = this.getChr_from();
        String strandFrom = this.getChr_from_strand();
        String bkptFrom = this.getChr_from_bkpt();

        this.setGene_affected_by_bkpt_from(this.getGene_affected_by_bkpt_to());
        this.setChr_from(this.getChr_to());
        this.setChr_from_strand(this.getChr_to_strand());
        this.setChr_from_bkpt(this.getChr_to_bkpt());

        this.setGene_affected_by_bkpt_to(affectedFrom);
        this.setChr_to(chrFrom);
        this.setChr_to_strand(strandFrom);
        this.setChr_to_bkpt(bkptFrom);
    }

    // main method for stand alone testing
    public static void main(String...args) {
        String dataSourceUrl = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PACA-AU/structural_somatic_mutation.PACA-AU.tsv.gz";
        if(!IcgcUtil.isIcgcConnectionWorking()) {
            dataSourceUrl = "///Users/criscuof/Downloads/structural_somatic_mutation.PACA-AU.tsv.gz";
        }
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(dataSourceUrl)));
            String line = "";
            int lineCount = 0;
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0) {
                    IcgcFusionModel model = StringUtils.columnStringToObject(IcgcFusionModel.class,
                            line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(IcgcFusionModel.class));
                    System.out.println("original  gene: " +model.getGene() +" entrez id " +model.getEntrezGeneId()
                            +" chromosome " +model.getChr_from() +" start "
                            +model.getChr_from_bkpt() +" fusion "+ model.getFusion());
                    // test swap

                    model.swapToAndFromLocations();
                    System.out.println("swapped  gene: " +model.getGene() +" entrez id " +model.getEntrezGeneId()
                            +" chromosome " +model.getChr_from() +" start "
                            +model.getChr_from_bkpt() +" fusion "+ model.getFusion());

                }
            }
            System.out.println("line count " +lineCount);
        }catch (IOException | InvocationTargetException | NoSuchMethodException |  NoSuchFieldException
                    | InstantiationException  | IllegalAccessException e) {
                e.printStackTrace();
            }
    }

}

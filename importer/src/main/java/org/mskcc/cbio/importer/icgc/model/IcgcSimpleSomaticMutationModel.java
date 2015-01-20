package org.mskcc.cbio.importer.icgc.model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.icgc.support.IcgcSimpleSomaticRecord;
import org.mskcc.cbio.importer.icgc.support.IcgcUtil;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformation;
import org.mskcc.cbio.importer.persistence.staging.mutation.TransformableModel;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
public class IcgcSimpleSomaticMutationModel extends MutationModel {
    /*
    represents a MutationModel subclass that supports data from an ICGC Simple Somatic Mutation
    record.
     */

    public IcgcSimpleSomaticMutationModel() {}
    
    private String icgc_mutation_id;
     private String icgc_donor_id;
    private String project_code;
    private String icgc_specimen_id;
    private String icgc_sample_id;
    private String matched_icgc_sample_id;
    private String submitted_sample_id;
    private String submitted_matched_sample_id;
    private String chromosome;
    private String chromosome_start;
    private String chromosome_end;
    private String chromosome_strand;
    private String assembly_version;
    private String mutation_type;
    private String reference_genome_allele;
    private String mutated_from_allele;
    private String mutated_to_allele;
    private String quality_score;
    private String probability;
    private String total_read_count;
    private String mutant_allele_read_count;
    private String verification_status;
    private String verification_platform;
    private String biological_validation_status;
    private String biological_validation_platform;
    private String consequence_type;
    private String aa_mutation;	
    private String cds_mutation;
    private String gene_affected;
    private String transcript_affected;
    private String gene_build_version;
    private String platform;
    private String experimental_protocol;
    private String sequencing_strategy;
    private String base_calling_algorithm;
    private String alignment_algorithm;
    private String variation_calling_algorithm;
    private String other_analysis_algorithm;
    private String seq_coverage;
    private String raw_data_repository;
    private String raw_data_accession;
    private String initial_data_release_date;

    /*
    getters needed to support MutationModel contract - used for staging file output
     */
    @Override
    public String getGene() {
        return IcgcFunctionLibrary.resolveGeneSymbol.apply(this.getGene_affected());
    }

    @Override
    public String getEntrezGeneId() {
        return IcgcFunctionLibrary.resolveEntrezId.apply(this.getGene_affected());
    }

    @Override
    public String getCenter() {
        return this.getProject_code();
    }

    @Override
    public String getBuild() {
        return IcgcFunctionLibrary.resolveSimpleBuildNumber.apply(this.getAssembly_version());
    }

    @Override
    public String getChromosome() {
        return this.chromosome;
    }

    @Override
    public String getStartPosition() {
        return this.getChromosome_start();
    }

    @Override
    public String getEndPosition() {
        return this.getChromosome_end();
    }

    @Override
    public String getStrand() {
        return IcgcFunctionLibrary.resolveStrand.apply( this.getChromosome_strand());
    }

    @Override
    public String getVariantClassification() {
        return this.getConsequence_type();
    }

    @Override
    public String getVariantType() {
        return IcgcFunctionLibrary.resolveVariantType.apply(new Tuple2<String,String>(this.getReference_genome_allele(),
                this.getMutated_to_allele()));
    }

    @Override
    public String getRefAllele() {
        return this.getReference_genome_allele();
    }

    @Override
    public String getTumorAllele1() {
        return this.getMutated_to_allele();
    }

    @Override
    public String getTumorAllele2() {
        return this.getMutated_to_allele();
    }

    @Override
    public String getDbSNPRS() {
        return "";
    }

    @Override
    public String getDbSNPValStatus() {
        return "";
    }

    @Override
    public String getTumorSampleBarcode() {
        return this.getIcgc_sample_id();
    }

    @Override
    public String getMatchedNormSampleBarcode() {
        return this.submitted_matched_sample_id;
    }

    @Override
    public String getMatchNormSeqAllele1() {
        return this.getReference_genome_allele();

    }

    @Override
    public String getMatchNormSeqAllele2() {
        return this.getReference_genome_allele();
    }

    @Override
    public String getTumorValidationAllele1() {
        return this.getMutated_to_allele();
    }

    @Override
    public String getTumorValidationAllele2() {
        return this.getMutated_to_allele();
    }

    @Override
    public String getMatchNormValidationAllele1() {
        return "";
    }

    @Override
    public String getMatchNormValidationAllele2() {
        return "";
    }

    @Override
    public String getVerificationStatus() {
        return this.getVerification_status();
    }

    @Override
    public String getValidationStatus() {
        return this.getBiological_validation_status();
    }

    @Override
    public String getMutationStatus() {
        return this.getBiological_validation_status();
    }

    @Override
    public String getSequencingPhase() {
        return "";
    }

    @Override
    public String getSequenceSource() {
        return this.sequencing_strategy;
    }

    @Override
    public String getValidationMethod() {
        return "";
    }

    @Override
    public String getScore() {
        return this.getQuality_score();
    }

    @Override
    public String getBAMFile() {
        return "";
    }

    @Override
    public String getSequencer() {
        return this.getPlatform();
    }

    @Override
    public String getTumorSampleUUID() {
        return this.getSubmitted_sample_id();
    }

    @Override
    public String getMatchedNormSampleUUID() {
        return this.getMatched_icgc_sample_id();

    }

    @Override
    public String getTAltCount() {
        return this.getMutant_allele_read_count();
    }

    @Override
    public String getTRefCount() {
        return IcgcFunctionLibrary.resolveVariantType.apply(new Tuple2<String,String>(this.getTotal_read_count(),
                this.getMutant_allele_read_count()));
    }

    @Override
    public String getNAltCount() {
        return "";
    }

    @Override
    public String getNRefCount() {
        return "";
    }

    @Override
    public String getAAChange() {
        return this.getAa_mutation();
    }

    @Override
    public String getTranscript() {
        return this.getCds_mutation();
    }

    /*
    getters and setters used for mapping from ICGC TSV file by column name
    used for input
     */
    public String getIcgc_mutation_id() {
        return icgc_mutation_id;
    }

    public void setIcgc_mutation_id(String icgc_mutation_id) {
        this.icgc_mutation_id = icgc_mutation_id;
    }

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

    public String getChromosome_strand() {
        return chromosome_strand;
    }

    public void setChromosome_strand(String chromosome_strand) {
        this.chromosome_strand = chromosome_strand;
    }

    public String getAssembly_version() {
        return assembly_version;
    }

    public void setAssembly_version(String assembly_version) {
        this.assembly_version = assembly_version;
    }

    public String getMutation_type() {
        return mutation_type;
    }

    public void setMutation_type(String mutation_type) {
        this.mutation_type = mutation_type;
    }

    public String getReference_genome_allele() {
        return reference_genome_allele;
    }

    public void setReference_genome_allele(String reference_genome_allele) {
        this.reference_genome_allele = reference_genome_allele;
    }

    public String getMutated_from_allele() {
        return mutated_from_allele;
    }

    public void setMutated_from_allele(String mutated_from_allele) {
        this.mutated_from_allele = mutated_from_allele;
    }

    public String getMutated_to_allele() {
        return mutated_to_allele;
    }

    public void setMutated_to_allele(String mutated_to_allele) {
        this.mutated_to_allele = mutated_to_allele;
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

    public String getTotal_read_count() {
        return total_read_count;
    }

    public void setTotal_read_count(String total_read_count) {
        this.total_read_count = total_read_count;
    }

    public String getMutant_allele_read_count() {
        return mutant_allele_read_count;
    }

    public void setMutant_allele_read_count(String mutant_allele_read_count) {
        this.mutant_allele_read_count = mutant_allele_read_count;
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

    public String getBiological_validation_status() {
        return biological_validation_status;
    }

    public void setBiological_validation_status(String biological_validation_status) {
        this.biological_validation_status = biological_validation_status;
    }

    public String getBiological_validation_platform() {
        return biological_validation_platform;
    }

    public void setBiological_validation_platform(String biological_validation_platform) {
        this.biological_validation_platform = biological_validation_platform;
    }

    public String getConsequence_type() {
        return consequence_type;
    }

    public void setConsequence_type(String consequence_type) {
        this.consequence_type = consequence_type;
    }

    public String getAa_mutation() {
        return aa_mutation;
    }

    public void setAa_mutation(String aa_mutation) {
        this.aa_mutation = aa_mutation;
    }

    public String getCds_mutation() {
        return cds_mutation;
    }

    public void setCds_mutation(String cds_mutation) {
        this.cds_mutation = cds_mutation;
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

    public String getSequencing_strategy() {
        return sequencing_strategy;
    }

    public void setSequencing_strategy(String sequencing_strategy) {
        this.sequencing_strategy = sequencing_strategy;
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

    public String getInitial_data_release_date() {
        return initial_data_release_date;
    }

    public void setInitial_data_release_date(String initial_data_release_date) {
        this.initial_data_release_date = initial_data_release_date;
    }

    //main method for standalone testing
    // requires either an Internet connection or a local ICGC file
    // reads data from a specific URL and invokes transformation
    // n.b. no duplicate record filtering

    public static void main (String...args){
        // read in a small ICGC simple somatic mutation data from URL or local file

        String dataSourceUrl = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PACA-AU/simple_somatic_mutation.open.PACA-AU.tsv.gz";
        if(!IcgcUtil.isIcgcConnectionWorking()) {
            dataSourceUrl = "///Users/criscuof/Downloads/simple_somatic_mutation.open.PACA-AU.tsv.gz";
        }
        List<String> lines = Lists.newArrayList();
        List<IcgcSimpleSomaticMutationModel> modelList = Lists.newArrayList();
        Path stagingFilePath = Paths.get("/tmp/icgctest/data_mutations_extended.txt");
        MutationFileHandlerImpl fileHandler = new MutationFileHandlerImpl();
        fileHandler.registerTsvStagingFile(stagingFilePath,
                IcgcFunctionLibrary.resolveColumnNames(MutationTransformation.INSTANCE.getTransformationMap()),true);
        System.out.println("Processing data from: " +dataSourceUrl);
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(dataSourceUrl)));
            String line = "";
            int lineCount = 0;
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0){
                    IcgcSimpleSomaticMutationModel model = StringUtils.columnStringToObject(IcgcSimpleSomaticMutationModel.class,
                            line, StagingCommonNames.tabPattern,IcgcFunctionLibrary.resolveFieldNames(IcgcSimpleSomaticMutationModel.class));
                    fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model), model.getTransformationFunction() );
                }
            }
            System.out.println(lineCount +" records processed");
            System.out.println("FINIS...");
        } catch (IOException  | InvocationTargetException | NoSuchMethodException |  NoSuchFieldException
                 | InstantiationException  | IllegalAccessException e) {
            e.printStackTrace();
        }
    }



}

package org.mskcc.cbio.icgc.support;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import java.util.Map;
import scala.Tuple2;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */
/**
 * responsible for supplying a Map of MAF attributes to ICSC attributes
 * refactor cod 10Sep2014
 * add support for a second (optional) argument for the transformation funnctions
 * that require a second parameter
 *
 * @author criscuof
 */
public class MafAttributeToIcgcAttributeMapSupplier implements Supplier<Map<String, Tuple2<String,Optional<String>>>> {
   public MafAttributeToIcgcAttributeMapSupplier() {}
   private static final Optional<String> absent = Optional.absent();
    @Override
    public Map<String, Tuple2<String,Optional<String>>> get() {
       Map<String, Tuple2<String,Optional<String>>> attributeMap = Maps.newConcurrentMap();
        attributeMap.put("Hugo_Symbol", new Tuple2<>( "gene_affected",absent)); //1
        attributeMap.put("Entrez_Gene_Id", new Tuple2<>( "gene_affected",absent)); //2
        attributeMap.put("Center", new Tuple2<>( "project_code",absent));//3
        attributeMap.put("NCBI_Build", new Tuple2<>( "assembly_version",absent)); //4
        attributeMap.put("Chromosome", new Tuple2<>( "chromosome",absent)); //5
        attributeMap.put("Start_Position", new Tuple2<>( "chromosome_start",absent)); //6
        attributeMap.put("End_Position", new Tuple2<>( "chromosome_end",absent));   //7
        attributeMap.put("Strand",  new Tuple2<>( "chromosome_strand",absent));   //8
        attributeMap.put("Variant_Classification", new Tuple2<>( "consequence_type",absent));  //9
        attributeMap.put("Variant_Type", new Tuple2<>( "reference_genome_allele",Optional.of("mutated_to_allele")));  //10
        attributeMap.put("Reference_Allele", new Tuple2<>( "reference_genome_allele",absent)); //11
        attributeMap.put("Tumor_Seq_Allele1", new Tuple2<>( "mutated_to_allele",absent)); //12
        attributeMap.put("Tumor_Seq_Allele2", new Tuple2<>( "mutated_to_allele",absent));//13
        attributeMap.put("dbSNP_RS", new Tuple2<>( "",absent));  //14
        attributeMap.put("dbSNP_Val_Status", new Tuple2<>( "",absent));//15
        //attributeMap.put("Tumor_Sample_Barcode", new Tuple2<>( "submitted_sample_id",absent)); //16
         attributeMap.put("Tumor_Sample_Barcode", new Tuple2<>( "icgc_sample_id",absent)); //16
        attributeMap.put("Matched_Norm_Sample_Barcode", new Tuple2<>( "submitted_matched_sample_id",absent)); //17
        attributeMap.put("Match_Norm_Seq_Allele1", new Tuple2<>( "reference_genome_allele",absent)); //18
        attributeMap.put("Match_Norm_Seq_Allele2", new Tuple2<>( "reference_genome_allele",absent)); //19
        attributeMap.put("Tumor_Validation_Allele1", new Tuple2<>( "mutated_to_allele",absent)); //20
        attributeMap.put("Tumor_Validation_Allele2", new Tuple2<>( "mutated_to_allele",absent)); //21
        attributeMap.put("Match_Norm_Validation_Allele1", new Tuple2<>( "",absent));  //22
        attributeMap.put("Match_Norm_Validation_Allele2", new Tuple2<>( "",absent)); //23
        attributeMap.put("Verification_Status", new Tuple2<>( "verification_status",absent)); //24
        attributeMap.put("Validation_Status", new Tuple2<>( "biological_validation_status",absent)); //25
       Tuple2<String, Optional<String>> put = attributeMap.put("Mutation_Status", new Tuple2<>( "biological_validation_status",absent)); //26
        attributeMap.put("Sequencing_Phase", new Tuple2<>( "",absent));  //27
        attributeMap.put("Sequence_Source", new Tuple2<>( "sequencing_strategy",absent)); //28
        attributeMap.put("Validation_Method", new Tuple2<>( "", absent));// 29
        attributeMap.put("Score", new Tuple2<>( "quality_score", absent));  //30
        attributeMap.put("BAM_File", new Tuple2<>( "", absent)); //31
        attributeMap.put("Sequencer", new Tuple2<>( "platform",absent));//32
       Tuple2<String, Optional<String>> put1 = attributeMap.put("Tumor_Sample_UUID", new Tuple2<>( "submitted_sample_id", absent)); //33
        attributeMap.put("Matched_Norm_Sample_UUID", new Tuple2<>( "matched_igc_sample_id",absent)); //34    
        attributeMap.put("t_alt_count", new Tuple2<>("mutant_allele_read_count",absent));  // new
        attributeMap.put("t_ref_count",new Tuple2<>( "total_read_count",Optional.of("mutant_allele_read_count"))); // new
         attributeMap.put("n_alt_count", new Tuple2<>( "",absent)); 
         attributeMap.put("n_ref_count", new Tuple2<>( "",absent));
        
        return attributeMap;

    }

}

package org.mskcc.cbio.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.CharMatcher;
import com.google.inject.internal.Lists;
import java.util.List;
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
 * responsible for supplying a Map of MAF attributes (keys) and appropriate
 * transformation functions
 *
 * @author criscuof
 */
public class MafTransformationFunctionMapSupplier implements Supplier<Map<String, Function<Tuple2<String, Optional<String>>, String>>> {

    private Map<String, Tuple2<String, String>> ensemblMap;

    public MafTransformationFunctionMapSupplier() {
        this.ensemblMap = Suppliers.memoize(new GeneNameMapSupplier()).get();
    }
    private static final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");

    @Override
    public Map< String, Function<Tuple2<String, Optional<String>>, String>> get() {
        Map< String, Function<Tuple2<String, Optional<String>>, String>> functionMap = Maps.newConcurrentMap();
        functionMap.put("Hugo_Symbol", getHugoFunction); //1
        functionMap.put("Entrez_Gene_Id", getEntrezFunction); //2
        functionMap.put("Center", copyAttribute);//3
        functionMap.put("NCBI_Build", resolveSimpleBuildNumber); //4
        functionMap.put("Chromosome", copyAttribute); //5
        functionMap.put("Start_Position", copyAttribute); //6
        functionMap.put("End_Position", copyAttribute);   //7
        functionMap.put("Strand", resolveStrand);   //8
        functionMap.put("Variant_Classification", copyAttribute);  //9
        functionMap.put("Variant_Type", resolveVariantType);  //10
        functionMap.put("Reference_Allele", copyAttribute); //11
        functionMap.put("Tumor_Seq_Allele1", copyAttribute); //12
        functionMap.put("Tumor_Seq_Allele2", copyAttribute);//13
        functionMap.put("dbSNP_RS", unsupported);  //14
        functionMap.put("dbSNP_Val_Status", unsupported);//15
        functionMap.put("Tumor_Sample_Barcode", copyAttribute); //16
        functionMap.put("Matched_Norm_Sample_Barcode", unsupported); //17
        functionMap.put("Match_Norm_Seq_Allele1", copyAttribute); //18
        functionMap.put("Match_Norm_Seq_Allele2", copyAttribute); //19
        functionMap.put("Tumor_Validation_Allele1", copyAttribute); //20
        functionMap.put("Tumor_Validation_Allele2", copyAttribute); //21
        functionMap.put("Match_Norm_Validation_Allele1", unsupported);  //22
        functionMap.put("Match_Norm_Validation_Allele2", unsupported); //23
        functionMap.put("Verification_Status", unsupported); //24
        functionMap.put("Validation_Status", resolveValidationStatus); //25
        functionMap.put("Mutation_Status", resolveMutationStatus); //26
        functionMap.put("Sequencing_Phase", unsupported);  //27
        functionMap.put("Sequence_Source", copyAttribute); //28
        functionMap.put("Validation_Method", unsupported);// 29
        functionMap.put("Score", copyAttribute);  //30
        functionMap.put("BAM_File", unsupported); //31
        functionMap.put("Sequencer", copyAttribute);//32
        functionMap.put("Tumor_Sample_UUID", copyAttribute);//33
        functionMap.put("Matched_Norm_Sample_UUID", copyAttribute); //34
        functionMap.put("t_alt_count", copyAttribute);  // new
        functionMap.put("t_ref_count", resolveReferenceCount); // new
        functionMap.put("n_alt_count", unsupported); 
         functionMap.put("n_ref_count", unsupported);
        return functionMap;
    }

    /*
     function to calculate the reference allele count
     parameter 1 = total allele count
     parameter 2 mutant allele count
     */
    Function<Tuple2<String, Optional<String>>, String> resolveReferenceCount
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1) && f._2.isPresent() && !Strings.isNullOrEmpty(f._2.get())) {
                        return new Integer(Integer.valueOf(f._1) - Integer.valueOf(f._2.get()))
                        .toString();
                    }
                    return "";
                }
            };
    
    /*
    function to remove GRCh prefix from ICGC build value (GRCh37 -> 37)
    */
    Function<Tuple2<String, Optional<String>>, String> resolveSimpleBuildNumber = 
            new Function<Tuple2<String, Optional<String>>, String> () {
        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
                    if (null != f._1) {
                        return CharMatcher.DIGIT.retainFrom(f._1);
                    }
                    return "";

                }
         
    };

    Function<Tuple2<String, Optional<String>>, String> resolveVariantType
            = new Function<Tuple2<String, Optional<String>>, String>() {
                /*
                 determine variant type
                 parm 1 is reference_genome_allele
                 parm2 is mutated_to_allele
                 both are required
                 */
                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1) && f._2.isPresent()) {
                        String refAllele = f._1;
                        String altAllele = f._2.get();
                        if (refAllele.equals("-")) {
                            return variationList.get(0);
                        }
                        if (altAllele.equals("-") || altAllele.length() < refAllele.length()) {
                            return "DEL";
                        }
                        if( refAllele.equals("-") || refAllele.length() < altAllele.length()) {
                            return "INS";
                        }
                        if (refAllele.length() < variationList.size()) {
                            return variationList.get(refAllele.length());
                        }
                    }
                    return "UNK";
                }

            };

    /*
    function to supply HUGO gene symbol via a table lookup
    */
    Function<Tuple2<String, Optional<String>>, String> getHugoFunction = 
            new Function<Tuple2<String, Optional<String>>, String>() {

        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
            if (!Strings.isNullOrEmpty(f._1)){
               Tuple2<String, String> nameTuple = ensemblMap.get(f._1);
                return (null == nameTuple || Strings.isNullOrEmpty(nameTuple._1)) ? "" : nameTuple._1;
            }
            return "";
        }

    };

    Function<Tuple2<String, Optional<String>>, String> getEntrezFunction = 
            new Function<Tuple2<String, Optional<String>>, String>() {
        // n.b. we have two Tuple2 objects here
        // f is a tuple of the arguments to this function
        // nameTuple is a tuple of hugo and entrez names
        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
             if (!Strings.isNullOrEmpty(f._1)){
                 Tuple2<String, String> nameTuple = ensemblMap.get(f._1);
            return (null == nameTuple || Strings.isNullOrEmpty(nameTuple._2)) ? "" : nameTuple._2;
             }
             return "";
        }

    };

    Function<Tuple2<String, Optional<String>>, String> copyAttribute
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                /*
                 simple copy of ICGC attribute to MAF file
                 */
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (null != f._1) {
                        return f._1;
                    }
                    return "";

                }

            };
    /*
    function to provide an empty string for unsupported MAF columns
    */
    Function<Tuple2<String, Optional<String>>, String> unsupported
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    return "";
                }

            };

    Function<Tuple2<String, Optional<String>>, String>  resolveStrand = 
            new Function<Tuple2<String, Optional<String>>, String> () {

        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
            if (!Strings.isNullOrEmpty(f._1)){
                return (f._1.equals("1")) ? "+" : "-";
            }
            return "";
        }

    };
    /*
    function to map ICGC validation value to MAF value
    */
    Function<Tuple2<String, Optional<String>>, String> resolveValidationStatus = 
            new Function<Tuple2<String, Optional<String>>, String>() {
       
        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
            if (!Strings.isNullOrEmpty(f._1)) {
                if(f._1.equals("valid")){
                    return "Valid";
                }
                
                if (f._1.equals("not tested")){
                    return "Untested";
                }
               
            }
            return "Invalid";
        }

    };
    
    
Function<Tuple2<String, Optional<String>>, String> resolveMutationStatus =
        new Function<Tuple2<String, Optional<String>>, String>() {

       
        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
             if (!Strings.isNullOrEmpty(f._1)) {
               return (f._1.contains("valid")) ? "Unknown" : "None"; 
            }
            return "None";
        }
    };

    Function<Tuple2<String, Optional<String>>, String> resolveVerificationStatus = 
            new Function<Tuple2<String, Optional<String>>, String>() {

            @Override
            public String apply(Tuple2<String, Optional<String>> f) {
                if( !Strings.isNullOrEmpty(f._1)){
                    return (f._1.contains("verified")) ? "Verified" : "Unknown";
                }
                return "Unknown";
            }

    };

}

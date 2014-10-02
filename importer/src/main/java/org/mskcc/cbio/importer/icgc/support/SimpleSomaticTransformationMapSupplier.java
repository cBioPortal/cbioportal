package org.mskcc.cbio.importer.icgc.support;

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
import scala.Tuple3;

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
 * transformation functions and function parameters from an ICGC Simple Somatic Mutation
 * record
 * The MAF attribute names are prefixed with numeric values to facilitate their ordering
 * The numbers are filtered out for output
 *
 * @author criscuof
 */
public class SimpleSomaticTransformationMapSupplier implements Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> {

    private Map<String, Tuple2<String, String>> ensemblMap;
    private final Supplier<Map<String, Tuple2<String, String>>> geneMapSUpplier
            = Suppliers.memoize(new GeneNameMapSupplier());

    public SimpleSomaticTransformationMapSupplier() {
        this.ensemblMap = geneMapSUpplier.get();
    }
    private static final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");
    private static final Optional<String> absent = Optional.absent();

    @Override
    public Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> get() {
        Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> transformationMap = Maps.newTreeMap();

        transformationMap.put("01Hugo_Symbol", new Tuple3<>(getHugoFunction, "gene_affected", absent)); //1
        transformationMap.put("02Entrez_Gene_Id", new Tuple3<>(getEntrezFunction, "gene_affected", absent)); //2
        transformationMap.put("03Center", new Tuple3<>(copyAttribute, "project_code", absent));//3
        transformationMap.put("04NCBI_Build", new Tuple3<>(resolveSimpleBuildNumber, "assembly_version", absent)); //4
        transformationMap.put("05Chromosome", new Tuple3<>(copyAttribute, "chromosome", absent)); //5
        transformationMap.put("06Start_Position", new Tuple3<>(copyAttribute, "chromosome_start", absent)); //6
        transformationMap.put("07End_Position", new Tuple3<>(copyAttribute, "chromosome_end", absent));   //7
        transformationMap.put("08Strand", new Tuple3<>(resolveStrand, "chromosome_strand", absent));   //8
        transformationMap.put("09Variant_Classification", new Tuple3<>(copyAttribute, "consequence_type", absent));  //9
        transformationMap.put("10Variant_Type", new Tuple3<>(resolveVariantType, "reference_genome_allele", Optional.of("mutated_to_allele")));  //10
        transformationMap.put("11Reference_Allele", new Tuple3<>(copyAttribute, "reference_genome_allele", absent)); //11
        transformationMap.put("12Tumor_Seq_Allele1", new Tuple3<>(copyAttribute, "mutated_to_allele", absent)); //12
        transformationMap.put("13Tumor_Seq_Allele2", new Tuple3<>(copyAttribute, "mutated_to_allele", absent));//13
        transformationMap.put("14dbSNP_RS", new Tuple3<>(unsupported, "", absent));  //14
        transformationMap.put("15dbSNP_Val_Status", new Tuple3<>(unsupported, "", absent));//15
        transformationMap.put("16Tumor_Sample_Barcode", new Tuple3<>(copyAttribute, "icgc_sample_id", absent)); //16
        transformationMap.put("176Matched_Norm_Sample_Barcode", new Tuple3<>(copyAttribute, "submitted_matched_sample_id", absent)); //17
        transformationMap.put("18Match_Norm_Seq_Allele1", new Tuple3<>(copyAttribute, "reference_genome_allele", absent)); //18
        transformationMap.put("198Match_Norm_Seq_Allele2", new Tuple3<>(copyAttribute, "reference_genome_allele", absent)); //19
        transformationMap.put("20Tumor_Validation_Allele1", new Tuple3<>(copyAttribute, "mutated_to_allele", absent)); //20
        transformationMap.put("21Tumor_Validation_Allele2", new Tuple3<>(copyAttribute, "mutated_to_allele", absent)); //21
        transformationMap.put("22Match_Norm_Validation_Allele1", new Tuple3<>(unsupported, "", absent));  //22
        transformationMap.put("23Match_Norm_Validation_Allele2", new Tuple3<>(unsupported, "", absent)); //23
        transformationMap.put("24Verification_Status", new Tuple3<>(copyAttribute, "verification_status", absent)); //24
        transformationMap.put("25Validation_Status", new Tuple3<>(resolveValidationStatus, "biological_validation_status", absent)); //25
        transformationMap.put("26Mutation_Status", new Tuple3<>(resolveMutationStatus, "biological_validation_status", absent)); //26
        transformationMap.put("27Sequencing_Phase", new Tuple3<>(unsupported, "", absent));  //27
        transformationMap.put("28Sequence_Source", new Tuple3<>(copyAttribute, "sequencing_strategy", absent)); //28
        transformationMap.put("29Validation_Method", new Tuple3<>(unsupported, "", absent));// 29
        transformationMap.put("30Score", new Tuple3<>(copyAttribute, "quality_score", absent));  //30
        transformationMap.put("31BAM_File", new Tuple3<>(unsupported, "", absent)); //31
        transformationMap.put("32Sequencer", new Tuple3<>(copyAttribute, "platform", absent));//32
        transformationMap.put("33Tumor_Sample_UUID", new Tuple3<>(copyAttribute, "submitted_sample_id", absent));//33
        transformationMap.put("34Matched_Norm_Sample_UUID", new Tuple3<>(copyAttribute, "matched_igc_sample_id", absent)); //34
        transformationMap.put("35t_alt_count", new Tuple3<>(copyAttribute, "mutant_allele_read_count", absent));  // new
        transformationMap.put("36t_ref_count", new Tuple3<>(resolveReferenceCount, "total_read_count", Optional.of("mutant_allele_read_count"))); // new
        transformationMap.put("37n_alt_count", new Tuple3<>(unsupported, "", absent));  // new
        transformationMap.put("38n_ref_count", new Tuple3<>(unsupported, "", absent));  //new
        return transformationMap;
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
    Function<Tuple2<String, Optional<String>>, String> resolveSimpleBuildNumber
            = new Function<Tuple2<String, Optional<String>>, String>() {
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
                        if (refAllele.equals("-") || refAllele.length() < altAllele.length()) {
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
    Function<Tuple2<String, Optional<String>>, String> getHugoFunction
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
                        Tuple2<String, String> nameTuple = ensemblMap.get(f._1);
                        return (null == nameTuple || Strings.isNullOrEmpty(nameTuple._1)) ? "" : nameTuple._1;
                    }
                    return "";
                }

            };

    Function<Tuple2<String, Optional<String>>, String> getEntrezFunction
            = new Function<Tuple2<String, Optional<String>>, String>() {
        // n.b. we have two Tuple2 objects here
                // f is a tuple of the arguments to this function
                // nameTuple is a tuple of hugo and entrez names
                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
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

    Function<Tuple2<String, Optional<String>>, String> resolveStrand
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
                        return (f._1.equals("1")) ? "+" : "-";
                    }
                    return "";
                }

            };
    /*
     function to map ICGC validation value to MAF value
     */
    Function<Tuple2<String, Optional<String>>, String> resolveValidationStatus
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
                        if (f._1.equals("valid")) {
                            return "Valid";
                        }

                        if (f._1.equals("not tested")) {
                            return "Untested";
                        }

                    }
                    return "Invalid";
                }

            };

    Function<Tuple2<String, Optional<String>>, String> resolveMutationStatus
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
                        return (f._1.contains("valid")) ? "Unknown" : "None";
                    }
                    return "None";
                }
            };

    Function<Tuple2<String, Optional<String>>, String> resolveVerificationStatus
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (!Strings.isNullOrEmpty(f._1)) {
                        return (f._1.contains("verified")) ? "Verified" : "Unknown";
                    }
                    return "Unknown";
                }

            };

}

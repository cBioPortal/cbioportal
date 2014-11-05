package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.dmp.util.EntrezIDSupplier;
import org.mskcc.cbio.importer.foundation.support.CommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationUtils;
import scala.Tuple2;
import scala.Tuple3;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fcriscuo on 11/2/14.
 */
public class FoundationMutationsTransformationMapSupplier implements

        Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> {
    private final Supplier<Map<String, String>> entrezIDSupplier = Suppliers.memoize(new EntrezIDSupplier());
    private Map<String, String> entrezMap = entrezIDSupplier.get();
    private final Optional<String> absent = Optional.absent();
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private static final Splitter posSplitter = Splitter.on(':');
    private static final Splitter blankSplitter = Splitter.on(' ').omitEmptyStrings();
    private static final Logger logger = Logger.getLogger(FoundationMutationsTransformationMapSupplier.class);

    @Override
    public Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> get() {
        Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> transformationMap = Maps.newTreeMap();
        transformationMap.put("001Hugo_Symbol", new Tuple3<>(copyAttribute, "getGene", absent)); //1
        transformationMap.put("002Entrez_Gene_Id", new Tuple3<>(getEntrezIDFunction, "getGene", absent)); //2
        transformationMap.put("003Center", new Tuple3<>(getCenter, "getValue", absent)); //3
        transformationMap.put("004Build", new Tuple3<>(getBuild, "getValue", absent)); //4
        transformationMap.put("005Chromosome", new Tuple3<>(getChromosome, "getPosition", absent)); //4
        transformationMap.put("006Start_Position", new Tuple3<>(getStartPosition, "getPosition", absent));
        transformationMap.put("007End_Position", new Tuple3<>(calculateEndPosition, "getCdsEffect", Optional.of("getPosition"))); //7
        transformationMap.put("008Strand", new Tuple3<>(copyAttribute, "getStrand", absent)); //8
        transformationMap.put("009Variant_Classification", new Tuple3<>(copyAttribute,"getFunctionalEffect",absent));
        transformationMap.put("010Variant_Type",new Tuple3<>(unsupported,"getFunctionalEffect",absent));
        transformationMap.put("011Ref_Allele", new Tuple3<>(getRefAllele,"getCdsEffect",Optional.of("getStrand")));
        transformationMap.put("012Tumor_Allele1", new Tuple3<>(getTumorAllele1,"getCdsEffect",Optional.of("getStrand")));
        transformationMap.put("013Tumor_Allele2", new Tuple3<>(getTumorAllele1,"getCdsEffect",Optional.of("getStrand")));
        transformationMap.put("014dbSNP_RS", new Tuple3<>(unsupported,"getFunctionalEffect",absent));
        transformationMap.put("015dbSNP_Val_Status", new Tuple3<>(unsupported,"getFunctionalEffect",absent));
        transformationMap.put("016Tumor_Sample_Barcode", new Tuple3<>(copyAttribute, "getValue", absent));
        transformationMap.put("017Matched_Norm_Sample_Barcode", new Tuple3<>(unsupported, "getValue", absent)); //17
        transformationMap.put("018Match_Norm_Seq_Allele1", new Tuple3<>(unsupported, "getValue", absent)); //18
        transformationMap.put("018Match_Norm_Seq_Allele2", new Tuple3<>(unsupported, "getValue", absent)); //19
        transformationMap.put("020Tumor_Validation_Allele1", new Tuple3<>(unsupported, "getValue", absent)); //20
        transformationMap.put("021Tumor_Validation_Allele2", new Tuple3<>(unsupported, "getValue", absent)); //21
        transformationMap.put("022Match_Norm_Validation_Allele1", new Tuple3<>(unsupported, "getValue", absent));  //22
        transformationMap.put("023Match_Norm_Validation_Allele2", new Tuple3<>(unsupported, "getValue", absent)); //23
        transformationMap.put("024Verification_Status", new Tuple3<>(unsupported, "getValue", absent)); //24
        transformationMap.put("025Validation_Status", new Tuple3<>(getDefaultStatus, "getValue", absent)); //25
        transformationMap.put("026Mutation_Status", new Tuple3<>(getDefaultStatus, "getValue", absent)); //26
        transformationMap.put("027Sequencing_Phase", new Tuple3<>(unsupported, "getValue", absent));  //27
        transformationMap.put("028Sequence_Source", new Tuple3<>(unsupported, "getValue", absent)); //28
        transformationMap.put("029Validation_Method", new Tuple3<>(unsupported, "getValue", absent));// 29
        transformationMap.put("030Score", new Tuple3<>(unsupported, "getValue", absent));  //30
        transformationMap.put("031BAM_File", new Tuple3<>(unsupported, "getValue", absent)); //31
        transformationMap.put("032Sequencer", new Tuple3<>(unsupported, "getGene", absent));//32
        transformationMap.put("033Tumor_Sample_UUID", new Tuple3<>(unsupported, "getValue", absent));//33
        transformationMap.put("034Matched_Norm_Sample_UUID", new Tuple3<>(unsupported, "getValue", absent)); //34
        transformationMap.put("035t_alt_count", new Tuple3<>(getTumorAltCount, "getDepth", Optional.of("getPercentReads")));  // new
        transformationMap.put("036t_ref_count", new Tuple3<>(getTumorRefCount, "getDepth", Optional.of("getPercentReads"))); // new
        transformationMap.put("037n_alt_count", new Tuple3<>(unsupported, "getValue", absent));  // new
        transformationMap.put("038n_ref_count", new Tuple3<>(unsupported, "getValue", absent));  //new
        return transformationMap;
    }



    Function<Tuple2<String, Optional<String>>, String> getTumorRefCount =
            new Function<Tuple2<String, Optional<String>>, String>() {
                public String apply(Tuple2<String, Optional<String>> f) {
                    final Long depth = Long.valueOf(f._1());
                    final Float percentReads = Float.valueOf(f._2().get());
                    final Long  tumorAltCount=  Math.round( depth * (percentReads/100.0));
                    return Long.toString( (long) depth - tumorAltCount);

                }
            };

    Function<Tuple2<String, Optional<String>>, String> getTumorAltCount =
            new Function<Tuple2<String, Optional<String>>, String>() {
                public String apply(Tuple2<String, Optional<String>> f) {
                    final Float depth = Float.valueOf(f._1());
                    final Float percentReads = Float.valueOf(f._2().get());
                    return Long.toString(Math.round( depth * (percentReads/100.0)));

                }
            };

    /*
    function to get tumor allele 1
     */

    Function<Tuple2<String, Optional<String>>, String> getTumorAllele1 =
            new Function<Tuple2<String, Optional<String>>, String>() {
                public String apply(Tuple2<String, Optional<String>> f) {
                    String cdsEffect = f._1();
                    final String strand = f._2.get();

                    String bases = cdsEffect.replaceAll("[^tcgaTCGA]", " ").trim();
                    // check for cdsEffects without nucleotides

                    List<String> alleleList = FluentIterable
                            .from(blankSplitter.split(bases))
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    if (!(strand.equals(CommonNames.MINUS_STRAND))) {
                                        return input.toUpperCase();
                                    }
                                    return FoundationUtils.INSTANCE.getCompliment(input.toUpperCase());
                                }
                            })
                            .toList();

                    if (alleleList.size() > 1) {
                        return alleleList.get(1);
                    } else if (cdsEffect.contains("ins")) {
                        return alleleList.get(0);
                    } else if (cdsEffect.contains("del")) {
                        return "-";
                    }
                    return "";

                }

                ;
            };

    /*
    function to get ref allele
     */
    Function<Tuple2<String, Optional<String>>, String> getRefAllele =
            new Function<Tuple2<String, Optional<String>>, String>() {
                public String apply(Tuple2<String, Optional<String>> f) {
                    final String cdsEffect = f._1();
                    final String strand = f._2.get();

                    String bases = cdsEffect.replaceAll("[^tcgaTCGA]", " ").trim();
                    // check for cdsEffects without nucleotides
                    if (cdsEffect.contains("ins")){
                        return "-";
                        }
                    if (Strings.isNullOrEmpty(bases)){
                        logger.info("No nucleotides in CDS: "+cdsEffect);
                        return "";
                    }
                    List<String> alleleList = FluentIterable
                            .from(blankSplitter.split(bases))
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    if (!(strand.equals(CommonNames.MINUS_STRAND))) {
                                        return input.toUpperCase();
                                    }
                                    return FoundationUtils.INSTANCE.getCompliment(input.toUpperCase());
                                }
                            })
                            .toList();


                    if (alleleList.size() > 0 ) {

                        return alleleList.get(0);
                    }
                    return "";

                }

                ;
            };

 /*
        function to resolve the chromosome value from the mutation position attribute
         */
    Function<Tuple2<String, Optional<String>>, String> getChromosome =
            new Function<Tuple2<String, Optional<String>>, String>() {
                public String apply(Tuple2<String, Optional<String>> f) {
                    String position = f._1();
                    return Lists.newArrayList(posSplitter.split(position).iterator()).get(0)
                            .replace(CommonNames.CHR_PREFIX, "");
                }
            };
/*
 function to calculate the end position based on the cds effect and the start position
 tuple has (1) cds_effect and (2) start position
 */

    Function<Tuple2<String, Optional<String>>, String> calculateEndPosition
            = new Function<Tuple2<String, Optional<String>>, String>() {

        @Override
        public String apply(Tuple2<String, Optional<String>> f) {
            String cdsEffect = f._1();
            Integer startPos = Integer.valueOf(Lists.newArrayList(posSplitter.split(f._2().get()).iterator()).get(1));
            String[] changes = cdsEffect.replaceAll("[^atcgATCG]", " ").trim().split(" "); // normally only 1
            if (cdsEffect.contains("del")  ) {

                if (changes.length > 1) {
                    return (Integer.valueOf(startPos + changes[1].length() - changes[0].length() - 1)).toString();
                }
                return (Integer.valueOf(startPos + changes[0].length()  - 1)).toString();
            }
            if (cdsEffect.contains("ins") ) {
                return (Integer.valueOf(startPos + 1)).toString();
            }
            if (cdsEffect.contains(">")) {

                if (changes.length > 0) {
                    return (Integer.valueOf(startPos + changes[0].length() - 1)).toString();
                }
            }

            logger.info("++++Unable to determine stop position for CDS effect " + cdsEffect);

            return startPos.toString();

        }
    };

        /*
        function to resolve the start value from the mutation position attribute
         */
        Function<Tuple2<String, Optional<String>>, String> getStartPosition =
                new Function<Tuple2<String, Optional<String>>, String>() {
                    public String apply(Tuple2<String, Optional<String>> f) {
                        String position = f._1();
                        return Lists.newArrayList(posSplitter.split(position).iterator()).get(1);
                    }
                };


        Function<Tuple2<String, Optional<String>>, String> copyAttribute
                = new Function<Tuple2<String, Optional<String>>, String>() {

            @Override
                    /*
                     simple copy of Foundation attribute to MAF file
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


        /*
                function to supply an Entrez ID based on a HUGO Symbol
                */
        Function<Tuple2<String, Optional<String>>, String> getEntrezIDFunction
                = new Function<Tuple2<String, Optional<String>>, String>() {

            @Override
            public String apply(Tuple2<String, Optional<String>> f) {
                if (!Strings.isNullOrEmpty(f._1)) {
                    return (Strings.isNullOrEmpty(entrezMap.get(f._1))) ? "" : entrezMap.get(f._1);
                }
                return "";
            }

        };

        Function<Tuple2<String, Optional<String>>, String> getCenter
                = new Function<Tuple2<String, Optional<String>>, String>() {

            @Override
            // for now return a default value
            public String apply(Tuple2<String, Optional<String>> f) {
                return DMPCommonNames.CENTER_MSKCC;

            }
        };

    Function<Tuple2<String, Optional<String>>, String> getDefaultStatus
            = new Function<Tuple2<String, Optional<String>>, String>() {

        @Override
        // for now return a default value
        public String apply(Tuple2<String, Optional<String>> f) {
            return CommonNames.DEFAULT_VALIDATION_STATUS;

        }
    };


    Function<Tuple2<String, Optional<String>>, String> getBuild
                = new Function<Tuple2<String, Optional<String>>, String>() {

            @Override
            // for now return a default value
            public String apply(Tuple2<String, Optional<String>> f) {
                return DMPCommonNames.DEFAULT_BUILD_NUMBER;

            }
        };


    }

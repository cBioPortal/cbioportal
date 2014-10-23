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
package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.DmpSnp;
import org.mskcc.cbio.importer.dmp.model.MetaData;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.dmp.util.EntrezIDSupplier;
import org.mskcc.cbio.importer.persistence.staging.MafFileHandler;
import scala.Tuple2;
import scala.Tuple3;

/*
 responsible for transforming data from the snp-exonic and snp-silent sections
 of a DMP data file 
 generates the data_mutations_extended.txt (MAF format) file
 */
public class DmpSnpTransformer implements DMPDataTransformable {

    private final MafFileHandler fileHandler;
    private final static Logger logger = Logger.getLogger(DmpSnpTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private static final String mutationsFileName = "data_mutations_extended.txt";

    private final Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> transformationMaprSupplier
            = Suppliers.memoize(new DMPMutationsTransformationMapSupplier());

    public DmpSnpTransformer(MafFileHandler aHandler, Path stagingDirectoryPath) {
        Preconditions.checkArgument(null != aHandler, "A MafFileHandler implementation is required");
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(stagingDirectoryPath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + stagingDirectoryPath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(stagingDirectoryPath),
                "The specified Path: " + stagingDirectoryPath + " is not writable");
        this.fileHandler = aHandler;
        // initialize the MAF file handler for DMP SNP data
        aHandler.registerMafStagingFile(stagingDirectoryPath.resolve(mutationsFileName),
                this.resolveColumnNames());
    }

    /*
     resolve the MAF file headings from the transformation map
     strip off the numeric prefix used to order key sets from the map
     */
    private List<String> resolveColumnNames() {
        return FluentIterable.from(this.transformationMaprSupplier.get().keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    @Override
    public void transform(DmpData data) {
        // the deprecated samples in the legacy data must be removed before appending 
        // the new samples
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        Set<String> processedSamples = this.fileHandler.resolveProcessedSampleSet(DMPCommonNames.SAMPLE_ID_COLUMN_NAME);
        Set<String> currentSamples = FluentIterable.from(data.getResults())
                .transform(new Function<Result, String>() {

                    @Override
                    public String apply(Result result) {
                        return result.getMetaData().getDmpSampleId().toString();
                    }
                }).toSet();
        Set<String> deprecatedSamples = Sets.intersection(processedSamples, currentSamples);
        // the set of samples for all mutations
        Set<String> totalMutationSamples = Sets.union(currentSamples, processedSamples);
        
        logger.info(deprecatedSamples.size() + " samples have been deprecated by new DMP data");
        logger.info(totalMutationSamples.size() +" total mutation samples");
        
        // remove any deprecated Samples
        if (!deprecatedSamples.isEmpty()) {
            this.fileHandler.removeDeprecatedSamplesFomMAFStagingFiles(DMPCommonNames.SAMPLE_ID_COLUMN_NAME, deprecatedSamples);
        }
        for (Result result : data.getResults()) {
            this.processSnps(result);
        }
    }

    /*
     private method to invoke transformation of SNP attributes to a tsv
     file. The two types of SNPs are combined into a single list.
     */
    private void processSnps(Result result) {
        List<DmpSnp> snpList = Lists.newArrayList();
        final MetaData meta = result.getMetaData();
        snpList.addAll(result.getSnpExonic());
        snpList.addAll(result.getSnpSilent());
        //add the sample id to each snp from the result metadata
        List<DmpSnp> transformedSnpList = FluentIterable.from(snpList)
                .transform(new Function<DmpSnp, DmpSnp>() {
                    @Override
                    public DmpSnp apply(DmpSnp snp) {
                        // add the sample id to the SNP
                        snp.setDmpSampleId(meta.getDmpSampleId());
                        return snp;
                    }
                }).toList();
        // invoke the transformation function on each snp and output to a file
        if (!transformedSnpList.isEmpty()) {
            this.fileHandler.transformImportDataToStagingFile(transformedSnpList, transformationFunction);
        }
    }

    /*
     Function to transform DMP SNP attributesfrom a DmpSnp object into MAF attributes collected in
     a tsv String for subsequent output
     */
    Function<DmpSnp, String> transformationFunction = new Function<DmpSnp, String>() {
        @Override
        public String apply(final DmpSnp snp) {
            Set<String> attributeList = transformationMaprSupplier.get().keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>> tuple3
                            = transformationMaprSupplier.get().get(attribute);
                            String attribute1 = DmpUtils.pojoStringGetter(tuple3._2(), snp);

                            Optional<String> optAttribute2 = (Optional<String>) ((tuple3._3().isPresent())
                                    ? Optional.of(DmpUtils.pojoStringGetter(tuple3._3().get(), snp))
                                    : Optional.absent());

                            return tuple3._1().apply(new Tuple2(attribute1, optAttribute2));

                        }
                    }).toList();
            String retRecord = tabJoiner.join(mafAttributes);

            return retRecord;
        }
   
    };

    
    /*
    Private inner class to encapsulate the DMP to MAF attribute transformations
    Implemented as a single item cache (i.e. Supplier)
    */
    private class DMPMutationsTransformationMapSupplier implements
            Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> {

        private final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");
        private final Optional<String> absent = Optional.absent();
        private final Supplier<Map<String, String>> entrezIDSupplier = Suppliers.memoize(new EntrezIDSupplier());
        private Map<String, String> entrezMap;

        public DMPMutationsTransformationMapSupplier() {
            this.entrezMap = entrezIDSupplier.get();
        }

        /*
         Map of MAF attributes and associated DMP to MAF attribute transformation functions
         These transformation functions utilize specified getter methods from the DMP model
         objects to resolve DMP attribute values
         The second Optional DMP argument is provided to support transformation functions that
         operate on two DMP attributes to determine a single MAF attribute
         */
        @Override
        public Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> get() {
            Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> transformationMap = Maps.newTreeMap();
            transformationMap.put("001Hugo_Symbol", new Tuple3<>(copyAttribute, "getGeneId", absent)); //1
            transformationMap.put("002Entrez_Gene_Id", new Tuple3<>(getEntrezIDFunction, "getGeneId", absent)); //2
            transformationMap.put("003Center", new Tuple3<>(getCenter, "getGeneId", absent)); //3
            transformationMap.put("004Build", new Tuple3<>(getBuild, "getGeneId", absent)); //4
            transformationMap.put("005Chromosome", new Tuple3<>(copyAttribute, "getChromosome", absent)); //5
            transformationMap.put("006Start_Position", new Tuple3<>(copyAttribute, "getStartPosition", absent));
            transformationMap.put("007End_Position", new Tuple3<>(calculateEndPosition, "getStartPosition", Optional.of("getRefAllele"))); //7
            transformationMap.put("008Strand", new Tuple3<>(getStrand, "getGeneId", absent)); //8
            transformationMap.put("009Variant_Classification", new Tuple3<>(copyAttribute, "getVariantClass", absent)); //9
            transformationMap.put("010Variant_Type", new Tuple3<>(resolveVariantType, "getRefAllele", Optional.of("getAltAllele"))); //10
            transformationMap.put("011Ref_Allele", new Tuple3<>(copyAttribute, "getRefAllele", absent)); //11
            transformationMap.put("012Tumor_Allele1", new Tuple3<>(copyAttribute, "getAltAllele", absent)); //12
            transformationMap.put("013Tumor_Allele2", new Tuple3<>(copyAttribute, "getAltAllele", absent)); //13
            transformationMap.put("014dbSNP_RS", new Tuple3<>(copyAttribute, "getDbSNPId", absent)); //14
            transformationMap.put("015dbSNP_Val_Status", new Tuple3<>(unsupported, "getGeneId", absent)); //15
            transformationMap.put("016Tumor_Sample_Barcode", new Tuple3<>(copyAttribute, "getDmpSampleId", absent)); //14
            return transformationMap;
        }

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

        Function<Tuple2<String, Optional<String>>, String> calculateEndPosition
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    public String apply(Tuple2<String, Optional<String>> tuple2) {
                        Integer start = Integer.valueOf(tuple2._1());
                        // calculate the stop position based on the length of
                        // the reference allele involved
                        Integer stop = start + tuple2._2().get().length() - 1;
                        return stop.toString();

                    }
                };

        Function<Tuple2<String, Optional<String>>, String> getStrand
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    // for now return a default value
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return DMPCommonNames.DEFAULT_STRAND;

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

        Function<Tuple2<String, Optional<String>>, String> getCenter
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    // for now return a default value
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return DMPCommonNames.CENTER_MSKCC;

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

    }

}

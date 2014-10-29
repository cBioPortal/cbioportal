package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.google.common.collect.Table;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import org.mskcc.cbio.importer.foundation.support.CasesTypeSupplier;
import org.mskcc.cbio.importer.foundation.support.CommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationUtils;
import org.mskcc.cbio.importer.util.EntrezIDSupplier;
import scala.Tuple2;

/*
 responsible for transforming one or more XML files representing Foundation
 studies in a directory to cbio staging files
 Transformation steps: 
 1. delete any existing staging files
 2. create new instances of staging files
 3. for each XML file within the directory
 a. unmarshal XML to JAXB object graph
 b. for each case in object graph
 i. filter out excluded cases
 ii. transform Java class attribute values to values in a staging file record
 iii. append staging file record to appropriate staging file
 4. close staging files
    
 */
public class FoundationXMLTransformer implements FileTransformer {

    private String xmlFilename;
    private static final Logger logger = Logger.getLogger(FoundationXMLTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private static final Splitter posSplitter = Splitter.on(':');
    private static final Splitter blankSplitter = Splitter.on(' ').omitEmptyStrings();
    private FoundationStagingFileManager fileManager;
    private Table<String, String, Integer> cnaTable;
    // component to map HUGO symbols to Entrez IDs
    private final Supplier<Map<String, String>> entrezIDSupplier = Suppliers.memoize(new EntrezIDSupplier());
    private   Map<String,String> entrezMap;

    private Supplier<CasesType> casesTypeSupplier;
    //private final String baseStagingDirectory;
    private final Config config;

    public FoundationXMLTransformer(Config aConfig) {
        Preconditions.checkArgument(null != aConfig, "A Config object is required");
        this.entrezMap = entrezIDSupplier.get();
        this.config = aConfig;
    }

    public FoundationXMLTransformer(String filename, String outDir) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(filename), "An XML filename is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(outDir), "An output directory is required");
        this.xmlFilename = filename;

        // add a Supplier to provide the top level JAXB object
        this.casesTypeSupplier = Suppliers.memoize(new CasesTypeSupplier(this.xmlFilename));
        //this.baseStagingDirectory = "";
        this.config = null;

    }
    /*
     mod 03Oct2014 - modify transformer to process all XML files within the
     specified Path (i.e. directory). The transformation types (e.g. mutations)
     from all files within the directory will be written to the same staging file
    
     This version also supports filtering out the excluded cases specified in
     the Config object
    
     Use the FoundationStagingFileManager inner class to handle I/O operations
     to the staging files
     */

    @Override
    public void transform(FileDataSource xmlSource) {
        Preconditions.checkArgument(null != xmlSource,
                "A FileDataSource for XML input files is required");
        Preconditions.checkArgument(!xmlSource.getFilenameList().isEmpty(),
                "The FileDataSource does not contain any XML files");
        // instantiate a new FoundationStagingFileManager
        this.fileManager
                = new FoundationStagingFileManager(Paths.get(xmlSource.getDirectoryName()));
        // the CNA table must be persisted across all the XML files in a study
        this.cnaTable = HashBasedTable.create();
        for (Path xmlPath : xmlSource.getFilenameList()) {
            this.casesTypeSupplier = Suppliers.memoize(new CasesTypeSupplier(xmlPath.toString()));
            this.processFoundationData();
        }
        // the CNA report can only be written after all the XML files have been processed
        this.generateCNAReport(cnaTable);
    }

    @Override
    public void transform(Path aPath) throws IOException {

    }

    private void processFoundationData() {

        this.generateMutationsDataReport();
        this.generateCNATable();
        this.generateClinicalDataReport();
        this.generateFusionDataReport();
    }

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


    Function<JAXBElement, Tuple2<String, Integer>> cnaFumction = new Function<JAXBElement, Tuple2<String, Integer>>() {
        @Override
        public Tuple2<String, Integer> apply(JAXBElement je) {
            CopyNumberAlterationType cna = (CopyNumberAlterationType) je.getValue();
            switch (cna.getType()) {
                case CommonNames.CNA_AMPLIFICATION:
                    return new Tuple2(cna.getGene(), 2);
                case CommonNames.CNA_LOSS:
                    return new Tuple2(cna.getGene(), -2);
                default:
                    return new Tuple2(cna.getGene(), 0);
            }
        }
    };

    private void generateCNATable() {
        CasesType casesType = this.casesTypeSupplier.get();

        for (CaseType ct : casesType.getCase()) {
            VariantReportType vrt = ct.getVariantReport();
            CopyNumberAlterationsType cnat = vrt.getCopyNumberAlterations();
            if (null != cnat) {
                for (Tuple2<String, Integer> cnaTuple : FluentIterable
                        .from(cnat.getContent())
                        .filter(JAXBElement.class)
                        .transform(cnaFumction)
                        .toList()) {
                    this.cnaTable.put(cnaTuple._1(), ct.getCase(), cnaTuple._2());
                }
            }
        }
    }

    /*
     write out the CNA table
     */
    private void generateCNAReport(Table<String, String, Integer> cnaTable) {
        Path cnaReportPath = this.fileManager.stagingReportWriterPath.get(CommonNames.CNA_REPORT_TYPE);
        try (BufferedWriter writer = Files.newBufferedWriter(
                cnaReportPath, Charset.defaultCharset())) {
            Set<String> geneSet = cnaTable.rowKeySet();
            Set<String> sampleSet = cnaTable.columnKeySet();
            // write out the headers
            writer.append(tabJoiner.join("Hugo_Symbol",
                    tabJoiner.join(sampleSet)) + "\n");
            for (String gene : geneSet) {
                String geneLine = gene;
                for (String sample : sampleSet) {
                    Integer value = (cnaTable.get(gene, sample) != null) ? cnaTable.get(gene, sample) : 0;
                    geneLine = tabJoiner.join(geneLine, value);
                }
                writer.append(geneLine + "\n");
            }

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    private String fusionCase;

    private void generateFusionDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();

        for (CaseType caseType : casesType.getCase()) {
            // put case id into global scope
            this.fusionCase = caseType.getCase();
            List<Serializable> contentList = caseType.getVariantReport().getRearrangements().getContent();
            if (contentList.size() > 0) {
                List<JAXBElement> jaxbList = Lists.newArrayList(Iterables.filter(contentList, JAXBElement.class));
                this.fileManager.generateDataReport(CommonNames.FUSION_REPORT_TYPE, jaxbList, rearrangementDataFunction);
            }
        }

    }
    /**
     * function to transform a collection of JAXBElement objects representing
     * RearrangementType XML elements into a collection of tab-delimited String
     * objects
     */
    Function<JAXBElement, String> rearrangementDataFunction = new Function<JAXBElement, String>() {
        @Override
        public String apply(JAXBElement je) {
            List<String> attributeList = Lists.newArrayList();
            RearrangementType rt = (RearrangementType) je.getValue();
            attributeList.add(rt.getTargetedGene());
            attributeList.add(""); // place holder for Entrez id
            attributeList.add(CommonNames.CENTER_FOUNDATION);
            attributeList.add(fusionCase);
            attributeList.add(rt.getTargetedGene() + "-" + rt.getOtherGene());
            attributeList.add(CommonNames.DEFAULT_FUSION);
            attributeList.add(CommonNames.DEFAULT_DNA_SUPPORT);
            attributeList.add(CommonNames.DEFAULT_RNA_SUPPORT);
            attributeList.add(CommonNames.DEFAULT_FUSION_METHOD);
            attributeList.add(parseFusionFrame(rt));
            return tabJoiner.join(attributeList);
        }

    };

    private String parseFusionFrame(RearrangementType rt) {
        if (Strings.isNullOrEmpty(rt.getInFrame()) || rt.getInFrame().equals(CommonNames.UNKNOWN)) {
            return CommonNames.UNKNOWN;
        }
        return (rt.getInFrame().equals("No")) ? CommonNames.OUT_OF_FRAME : CommonNames.IN_FRAME;
    }

    /**
     * Function to map attributes within a CaseType instance to a tab-delimited
     * String containing clinical data
     */
    Function<CaseType, String> clinicalDataFunction = new Function<CaseType, String>() {

        @Override
        public String apply(CaseType caseType) {
            List<String> attributeList = Lists.newArrayList();
            attributeList.add(caseType.getCase());
            attributeList.add(caseType.getVariantReport().getGender());
            attributeList.add(caseType.getFmiCase());
            attributeList.add(caseType.getVariantReport().getPipelineVersion());
            attributeList.add(displayMetricValue(caseType, CommonNames.METRIC_TUMOR_NUCLEI_PERCENT));  // not supported in current data
            attributeList.add(displayMetricValue(caseType, CommonNames.METRIC_MEDIAN_COVERAGE));
            attributeList.add(displayMetricValue(caseType, CommonNames.METRIC_COVERAGE_GT_100));
            //There are two (2) Error metrics; the first is the DNA error rate, the second is the RNA error rate
            // This application captures the first (i.e. DNA)
            attributeList.add(displayMetricValue(caseType, CommonNames.METRIC_ERROR));
            return tabJoiner.join(attributeList);
        }
    };

    /**
     * private method to generate the data_clinical.txt report
     *
     */
    private void generateClinicalDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();
        this.fileManager.generateDataReport(CommonNames.CLINICAL_REPORT_TYPE,
                casesType.getCase(), clinicalDataFunction);
    }
    /*
     private method to generate mutation report from short variant elements
     */

    private void generateMutationsDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();
        this.fileManager.generateDataReportFromFluentIterable(CommonNames.MUTATION_REPORT_TYPE, casesType.getCase(), shortVariantTypeFunction);
        //this.fileManager.generateDataReport(CommonNames.MUTATION_REPORT_TYPE, casesType.getCase(), shortVariantTypeFunction);
    }

    Function<CaseType, List<String>> shortVariantTypeFunction = new Function<CaseType, List<String>>() {
        @Override
        public List<String> apply(CaseType ct) {
            final String sample = ct.getCase(); // case is a reserved word use sample instead

            return FluentIterable.from(ct.getVariantReport().getShortVariants().getShortVariant())
                    .filter(new Predicate<ShortVariantType>() {

                        @Override
                        public boolean apply(ShortVariantType svt) {
                            return !Strings.isNullOrEmpty(svt.getGene());
                        }
                    })
                    .transform(new Function<ShortVariantType, String>() {
                        @Override
                   

                        public String apply(ShortVariantType svt) {
                            ChromosomePosition cp = new ChromosomePosition(svt.getPosition());
                            CdsEffect cdsEffect = new CdsEffect(svt.getCdsEffect(), svt.getFunctionalEffect(), svt.getStrand());
                            Integer end = cp.getStart() + cdsEffect.getLength() - 1;
                            List<String> attributeList = Lists.newArrayList();
                            // 1 HUGO SyMBOL
                            attributeList.add(svt.getGene());
                            // 2 entrez id
                            attributeList.add(getEntrezIDFunction.apply(new Tuple2(svt.getGene(),Optional.absent())));
                            //3 Center
                            attributeList.add(CommonNames.CENTER_FOUNDATION);
                            // 4 build
                            attributeList.add(CommonNames.BUILD);
                            // 5 chromsome
                            attributeList.add(cp.getChromosome());
                            // 6 start
                            attributeList.add(cp.getStart().toString());
                            // 7 end
                            attributeList.add(end.toString());
                            //8 strand
                            attributeList.add(svt.getStrand());
                            //9 variant classification
                            attributeList.add(svt.getFunctionalEffect());
                            // 10 variant type
                            attributeList.add("");
                            // 11 ref allele
                            attributeList.add(cdsEffect.getRefAllele());
                            // 12 tumor allele1
                            attributeList.add(cdsEffect.getTumorAllele1());
                            // 13 tumor allele2
                            attributeList.add(cdsEffect.getTumorAllele2());
                            // 14 dbSNP RS
                            attributeList.add("");
                            // 15 dbSNP valStatus
                            attributeList.add("");
                            // 16 sample barcode
                            attributeList.add(sample);
                            // 17 matched normal sample barcode
                            attributeList.add("");
                            // 18 matched normal allele1
                            attributeList.add("");
                            // 19 matched normal allele1
                            attributeList.add("");
                            // 20 tumor validation allele1
                            attributeList.add("");
                            // 21 tumor validation allele2
                            attributeList.add("");
                            // 22 match normal validation allele1
                            attributeList.add("");
                            // 23 match normal validation allele2
                            attributeList.add("");
                            // 24 verification
                            attributeList.add("");
                            // 25 validation status
                            attributeList.add(CommonNames.DEFAULT_VALIDATION_STATUS);  // unknown
                            // 26 mutation status
                            attributeList.add(CommonNames.DEFAULT_MUTATION_STATUS);   // unknown
                            // 27 sequencing phase
                            attributeList.add("");
                            // 28 sequencing source
                            attributeList.add("");
                            // 29 validation method
                            attributeList.add("");
                            // 30 score
                            attributeList.add("");
                            //31 BAM file
                            attributeList.add("");
                            // 32 sequencer
                            attributeList.add("");
                            // 33  "Tumor_Sample_UUID"
                            attributeList.add("");
                            // 34   "Matched_Norm_Sample_UUID"
                            attributeList.add("");
                            // amino acid change
                            //attributeList.add(svt.getProteinEffect());
                            // transcript
                            //attributeList.add(svt.getTranscript());
                            // 33 t_ref_count
                            attributeList.add(FoundationUtils.INSTANCE.displayTumorRefCount(svt));
                            // 34 t_alt_count
                            attributeList.add(FoundationUtils.INSTANCE.displayTumorAltCount(svt));
                            attributeList.add("\n");
                            return tabJoiner.join(attributeList);
                        }
                    }).toList();

        }
    };

    @Override
    /*
     * The primary identifier for Foundation Medicine cases is the study id
     */
    public String getPrimaryIdentifier() {
        CasesType casesType = this.casesTypeSupplier.get();
        // get the study from the first case
        return casesType.getCase().get(0).getVariantReport().getStudy();
    }

    @Override
    /*
     * the prrimary entity for Foundation Medicine XML data is the Case
     */
    public Integer getPrimaryEntityCount() {
        CasesType casesType = this.casesTypeSupplier.get();
        return casesType.getCase().size();
    }

    public String getStudyId() {
        CasesType casesType = this.casesTypeSupplier.get();
        // get the study from the first case
        return casesType.getCase().get(0).getVariantReport().getStudy();
    }

    /**
     * private method to provide a String representation of a Metric value
     *
     * @param caseType
     * @param metricName
     * @return
     */
    private String displayMetricValue(CaseType caseType, String metricName) {
        MetricsType metricsType = caseType.getVariantReport().getQualityControl().getMetrics();
        for (MetricType metric : metricsType.getMetric()) {
            if (metric.getName().equals(metricName)) {
                return metric.getMetricTypeValue();
            }
        }
        logger.info("Failed to find metric " + metricName + " for case " + caseType.getCase());
        return "";
    }

    /*
     inner class representing the components of the CDS effect data
     */
    public class CdsEffect {

        private Integer length;
        private Integer startPos;
        private Integer stopPos;
        private String refAllele;
        private String tumorAllele1;
        private String tumorAllele2;
        private final boolean plusStrand;

        public CdsEffect(String cdsEffect, String functionalEffect, String strand) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cdsEffect), "A CDS effect is required");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(functionalEffect), "A functional effect is required");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(strand), "A strand is required");
            Preconditions.checkArgument(strand.equals(CommonNames.PLUS_STRAND) || strand.equals(CommonNames.MINUS_STRAND),
                    "The stand argument must be either " + CommonNames.PLUS_STRAND + " or " + CommonNames.MINUS_STRAND);

            this.plusStrand = !(strand.equals(CommonNames.MINUS_STRAND));
            this.determinePositionsAndLength(cdsEffect);
            this.determineAlleles(cdsEffect);

        }
        /*
         * private method to parse the cds effect value to determine the reference and tumore alleles
         * current parctice is to set the value of tumorAllele2 to tumorAllele1
         */

        private void determineAlleles(String cdsEffect) {
            this.refAllele = "-";  // default value
            this.tumorAllele1 = "-";  // default value
            // retain only DNA nucleotides from cdsEffect
            String bases = cdsEffect.replaceAll("[^tcgaTCGA]", " ").trim();
            // check for cdsEffects without nucleotides
            if (Strings.isNullOrEmpty(bases)) {
                logger.error(cdsEffect + " is not a valid CDS Effect");
                return;
            }
            List<String> alleleList = FluentIterable
                    .from(blankSplitter.split(bases))
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            if (plusStrand) {
                                return input.toUpperCase();
                            }
                            return FoundationUtils.INSTANCE.getCompliment(input.toUpperCase());
                        }
                    })
                    .toList();

            if (alleleList.size() > 1) {
                this.refAllele = alleleList.get(0);
                this.tumorAllele1 = alleleList.get(1);
            } else if (cdsEffect.contains("ins")) {
                this.tumorAllele1 = alleleList.get(0);
            } else if (cdsEffect.contains("del")) {
                this.refAllele = alleleList.get(0);
            } else {
                logger.error("Unable to determine alleles for " + cdsEffect);
            }

            // set tumor allele 2 to the value of tumor allele 1
            this.tumorAllele2 = this.tumorAllele1;

        }

        private void determinePositionsAndLength(String cdsEffect) {

            String positions = cdsEffect.replaceAll("[^0123456789]", " ").trim();
            List<Integer> posList = FluentIterable
                    .from(blankSplitter.split(positions))
                    .transform(new Function<String, Integer>() {

                        @Override
                        public Integer apply(String input) {
                            return Integer.valueOf(input);
                        }
                    }
                    ).toList();
            this.startPos = (posList.isEmpty()) ? 0 : posList.get(0);
            this.stopPos = (posList.size() > 1) ? posList.get(1) : this.startPos;
            this.length = this.stopPos - this.startPos + 1;

        }

        public boolean isPlusStrand() {
            return this.plusStrand;
        }

        public Integer getLength() {
            return this.length;
        }

        public String getRefAllele() {
            return this.refAllele;
        }

        public String getTumorAllele1() {
            return this.tumorAllele1;
        }

        public String getTumorAllele2() {
            return this.tumorAllele2;
        }

    }

    /*
     a private inner class to manage interactions with the staging files
     for each study
     */
    private class FoundationStagingFileManager {

        private final Path stagingFilePath;
        private Map<String, Path> stagingReportWriterPath = Maps.newConcurrentMap();
        private Map<String, List<String>> reportHeadingsMap = Maps.newConcurrentMap();
        private final Map<String, String> reportFileMap
                = Maps.newConcurrentMap();

        /*
         BufferedWriter writer = Files.newBufferedWriter(
         aPath, Charset.defaultCharset()))
         */
        public FoundationStagingFileManager(Path aPath) {

            this.stagingFilePath = aPath;
            reportFileMap.put(CommonNames.MUTATION_REPORT_TYPE, "data_mutations_extended.txt");
            reportFileMap.put(CommonNames.CNA_REPORT_TYPE, "data_CNA.txt");
            reportFileMap.put(CommonNames.CLINICAL_REPORT_TYPE, "data_clinical.txt");
            reportFileMap.put(CommonNames.FUSION_REPORT_TYPE, "data_fusions.txt");

            reportHeadingsMap.put(CommonNames.MUTATION_REPORT_TYPE, Arrays.asList(CommonNames.MUTATIONS_REPORT_HEADINGS));
            reportHeadingsMap.put(CommonNames.CLINICAL_REPORT_TYPE, Arrays.asList(CommonNames.CLINICAL_DATA_HEADINGS));
            reportHeadingsMap.put(CommonNames.FUSION_REPORT_TYPE, Arrays.asList(CommonNames.FUSION_DATA_HEADINGS));
            this.resolveStagingReportWriterPathMap();
        }

        /*
         create a Map of report specific file paths, create the report files and
         write out any report headings
         */
        private void resolveStagingReportWriterPathMap() {
            for (Map.Entry<String, String> entry : reportFileMap.entrySet()) {
                try {
                    Path reportPath = Paths.get(pathJoiner.join(stagingFilePath.toString(), entry.getValue()));
                    this.createNewStagingFile(reportPath);
                    this.stagingReportWriterPath.put(entry.getKey(), reportPath);
                    // write out any report headings
                    if (reportHeadingsMap.containsKey(entry.getKey())) {
                        BufferedWriter writer = Files.newBufferedWriter(reportPath, Charset.defaultCharset());
                        writer.append(tabJoiner.join(reportHeadingsMap.get(entry.getKey())));
                        writer.newLine();
                        writer.flush();
                    }
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        /*
         special method to handle foundation mutations data
         */

        private void generateDataReportFromFluentIterable(String reportType,
                List<CaseType> dataList, Function fluentIterableFunction) {
            if (this.stagingReportWriterPath.containsKey(reportType)) {
                Path aPath = this.stagingReportWriterPath.get(reportType);
                OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};

                try {
                    List<String> outList = FluentIterable.from(dataList)
                            .transform(fluentIterableFunction)
                            .filter(new Predicate<List<String>>() {

                                @Override
                                public boolean apply(List<String> t) {
                                    return !t.isEmpty();
                                }
                            })
                            .transform(new Function<List<String>, String>() {

                                @Override
                                public String apply(List<String> f) {

                                    StringBuilder sb = new StringBuilder();
                                    for (String s : f) {
                                        if (!Strings.isNullOrEmpty(s) && s.length() > 1) {
                                            sb.append(s);
                                        }
                                    }
                                    return sb.toString();
                                }
                            })
                            .toList();

                    Files.write(aPath, outList, Charset.defaultCharset(), options);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            } else {
                logger.error(reportType + " is not a supported report type.");
            }
        }

        private void generateDataReport(String reportType, List aList, Function aFunction) {
            if (this.stagingReportWriterPath.containsKey(reportType)) {
                Path aPath = this.stagingReportWriterPath.get(reportType);
                OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
                // create the file if it doesn't exist, append to it if it does
                try {
                    Files.write(aPath, Lists.transform(aList, aFunction), Charset.defaultCharset(), options);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            } else {
                logger.error(reportType + " is not a supported report type.");
            }
        }

        /*
         create a new staging file for this study
         delete an older ones if it exist
         */
        private void createNewStagingFile(Path path) throws IOException {
            Files.deleteIfExists(path);
            Files.createFile(path); // accept default file attributes
            logger.info("Staging file " + path.toString() + " created");
        }
        /*
         method that will append a line to an existing report file based
         on the type parameter
         */

        void appendLineToRport(String type, String reportLine) {
            if (this.stagingReportWriterPath.containsKey(type)) {
                try (BufferedWriter writer = Files.newBufferedWriter(
                        this.stagingReportWriterPath.get(type), Charset.defaultCharset())) {
                    writer.append(reportLine);
                    writer.newLine();

                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }

    }

    public class ChromosomePosition {

        private String chromosome;
        private Integer start;

        public ChromosomePosition(String position) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(position), "A chromosome position is required");
            Preconditions.checkArgument(position.startsWith(CommonNames.CHR_PREFIX), "Invalid position " + position);
            Preconditions.checkArgument(position.contains(":"), "Invalid position " + position);
            this.setAttributes(position);
        }

        private void setAttributes(String position) {
            Iterator<String> is = posSplitter.split(position).iterator();
            this.chromosome = is.next().replace(CommonNames.CHR_PREFIX, "");
            this.start = Integer.valueOf(is.next());
        }

        public String getChromosome() {
            return this.chromosome;
        }

        public Integer getStart() {
            return this.start;
        }

    }

}

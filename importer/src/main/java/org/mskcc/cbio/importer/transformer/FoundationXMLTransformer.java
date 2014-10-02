package org.mskcc.cbio.importer.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.gdata.util.common.base.Pair;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.foundation.support.CasesTypeSupplier;
import org.mskcc.cbio.importer.foundation.support.CommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationUtils;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;

public class FoundationXMLTransformer implements FileTransformer {
    
    private String xmlFilename;
    private static final Logger logger = Logger.getLogger(FoundationXMLTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private static final Splitter posSplitter = Splitter.on(':');
    private static final Splitter blankSplitter = Splitter.on(' ').omitEmptyStrings();
    
    private FoundationStagingFileGenerator fileGenerator;
    private Supplier<CasesType> casesTypeSupplier;
    //private final String baseStagingDirectory;
    private final Config config;
    
    public FoundationXMLTransformer(Config aConfig) {
        
        Preconditions.checkArgument(null != aConfig, "A Config object is required");
        
       // this.baseStagingDirectory = baseDir;
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
    
    @Override
    public void transform(Path aPath) throws IOException {
        
        this.fileGenerator = new FoundationStagingFileGenerator(aPath);
        this.casesTypeSupplier = Suppliers.memoize(new CasesTypeSupplier(aPath.toString()));
        this.processFoundationData();
    }
    
    public void processFoundationData() {
        
        this.generateMutationsDataReport();
        this.generateCNATable();
        this.generateClinicalDataReport();
        this.generateFusionDataReport();
    }
    
    @Override
    /*
     * The primary identifier for Doundata Medicine cases is the study id
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
    
    Function<JAXBElement, Pair<String, Integer>> cnaFumction = new Function<JAXBElement, Pair<String, Integer>>() {
        @Override
        public Pair<String, Integer> apply(JAXBElement je) {
            CopyNumberAlterationType cna = (CopyNumberAlterationType) je.getValue();
            switch (cna.getType()) {
                case CommonNames.CNA_AMPLIFICATION:
                    return new Pair(cna.getGene(), 2);
                case CommonNames.CNA_LOSS:
                    return new Pair(cna.getGene(), -2);
                default:
                    return new Pair(cna.getGene(), 0);
            }
        }
        
    };
    
    private void generateCNATable() {
        CasesType casesType = this.casesTypeSupplier.get();
        Table<String, String, Integer> cnaTable = HashBasedTable.create();
        for (CaseType ct : casesType.getCase()) {
            VariantReportType vrt = ct.getVariantReport();
            CopyNumberAlterationsType cnat = vrt.getCopyNumberAlterations();
            if (null != cnat) {
                for (Pair<String, Integer> cnaPair : FluentIterable
                        .from(cnat.getContent())
                        .filter(JAXBElement.class)
                        .transform(cnaFumction)
                        .toList()) {
                    
                    cnaTable.put(cnaPair.getFirst(), ct.getCase(), cnaPair.getSecond());
                }
                
            }
            
        }
        
        this.generateCNAReport(cnaTable);
    }
    
  
    
    private void generateCNAReport(Table<String, String, Integer> cnaTable) {
        Optional<Path> optPath = this.fileGenerator.getCNAReportPath();
        Path cnaReportPath;
        if (optPath.isPresent()) {
            cnaReportPath = optPath.get();
        } else {
            logger.error("Failed to create output file for CNA report");
            return;
        }
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
    
    private String fusionCase;
    
    private void generateFusionDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();
        Optional<Path> optPath = this.fileGenerator.getFusionReportPath();
        Path fusionReportPath;
        if (optPath.isPresent()) {
            fusionReportPath = optPath.get();
        } else {
            logger.error("Failed to create output file for fusion report");
            return;
        }
        
        OpenOption[] options = new OpenOption[]{CREATE, APPEND};
        try (BufferedWriter writer = Files.newBufferedWriter(
                fusionReportPath, Charset.defaultCharset())) {
            writer.append(tabJoiner.join(CommonNames.FUSION_DATA_HEADINGS) + "\n");
            writer.flush();
            for (CaseType caseType : casesType.getCase()) {
                // put case id into global scope
                this.fusionCase = caseType.getCase();
                List<Serializable> contentList = caseType.getVariantReport().getRearrangements().getContent();
                if (contentList.size() > 0) {
                    List<JAXBElement> jaxbList = Lists.newArrayList(Iterables.filter(contentList, JAXBElement.class));
                    Files.write(fusionReportPath, Lists.transform(jaxbList, rearrangementDataFunction), Charset.defaultCharset(), options);
                }
            }
            
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    
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
     * @param casesType
     */
    private void generateClinicalDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();
        Optional<Path> optPath = this.fileGenerator.getClinicalDataReportPath();
        Path clinicalReportPath;
        if (optPath.isPresent()) {
            clinicalReportPath = optPath.get();
        } else {
            logger.error("Failed to create output file for clinical data report");
            return;
        }
        
        this.generateDataReport(clinicalReportPath, CommonNames.CLINICAL_DATA_HEADINGS,
                casesType.getCase(), clinicalDataFunction);
    }
    
    private void generateDataReport(Path aPath, String[] headings, List aList, Function aFunction) {
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        try (BufferedWriter writer = Files.newBufferedWriter(
                aPath, Charset.defaultCharset())) {
            writer.append(tabJoiner.join(headings) + "\n");
            writer.flush();
            Files.write(aPath, Lists.transform(aList, aFunction), Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    private void generateMutationsDataReport() {
        CasesType casesType = this.casesTypeSupplier.get();
        Optional<Path> optPath = this.fileGenerator.getMutationsReportPath();
        Path mutationReportPath;
        if (optPath.isPresent()) {
            mutationReportPath = optPath.get();
        } else {
            logger.error("Failed to create output file for mutations data report");
            return;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                mutationReportPath, Charset.defaultCharset())) {
            // write out the headers
            writer.append(tabJoiner.join(CommonNames.MUTATIONS_REPORT_HEADINGS) + "\n");
            for (CaseType ct : casesType.getCase()) {
                // case is a Java reserved term, use sample instead
                String sample = ct.getCase();
                // process variants
                VariantReportType vrt = ct.getVariantReport();
                if (null != vrt) {
                    ShortVariantsType svts = vrt.getShortVariants();
                    if (null != svts) {
                        List<ShortVariantType> svtList = svts.getShortVariant();
                        for (ShortVariantType svt : svtList) {
                            writer.append(this.processShortVariantType(sample, svt) + "\n");
                        }
                        
                    }
                }
            }
        } catch (IOException ex) {
            
        }
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

    /**
     * private method to map the attributes in a ShortVariantType to a
     * tab-delimited line for the data_mutations_extended.txt file
     *
     * @param svt
     * @return
     */
    private String processShortVariantType(String sample, ShortVariantType svt) {
        ChromosomePosition cp = new ChromosomePosition(svt.getPosition());
        CdsEffect cdsEffect = new CdsEffect(svt.getCdsEffect(), svt.getFunctionalEffect(), svt.getStrand());
        Integer end = cp.getStart() + cdsEffect.getLength() - 1;
        
        List<String> attributeList = Lists.newArrayList();
        attributeList.add(svt.getGene());
        attributeList.add(CommonNames.CENTER_FOUNDATION);
        attributeList.add(CommonNames.BUILD);
        attributeList.add(cp.getChromosome());
        attributeList.add(cp.getStart().toString());
        attributeList.add(end.toString());
        attributeList.add(svt.getStrand());
        attributeList.add(svt.getFunctionalEffect());
        attributeList.add(cdsEffect.getRefAllele());
        attributeList.add(cdsEffect.getTumorAllele1());
        attributeList.add(cdsEffect.getTumorAllele2());
        attributeList.add(sample);
        attributeList.add(CommonNames.DEFAULT_TUMOR_SAMPLE_BARCODE);  // unknown
        attributeList.add(CommonNames.DEFAULT_VALIDATION_STATUS);   // unknown
        attributeList.add(svt.getProteinEffect());
        attributeList.add(svt.getTranscript());
        attributeList.add(FoundationUtils.INSTANCE.displayTumorRefCount(svt));
        attributeList.add(FoundationUtils.INSTANCE.displayTumorAltCount(svt));
        return tabJoiner.join(attributeList);
        
    }
    
  
    /*
    inner class representing the componenst of the CDS effect data
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
            
            this.plusStrand = (strand.equals(CommonNames.MINUS_STRAND)) ? false : true;
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
            if(Strings.isNullOrEmpty(bases)){
                logger.error(cdsEffect +" is not a valid CDS Effect");
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
                logger.error("Unable to determine alleles for " +cdsEffect);
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
    
    public class FoundationStagingFileGenerator {
        
        private static final String mutationsReportName = "data_mutations_extended.txt";
        private static final String cnaReportName = "data_CNA.txt";
        private static final String clinicalReportName = "data_clinical.txt";
        private static final String fusionReportName = "data_fusions.txt";
        private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
        private  Path baseStagingPath;
        private Path stagingDirectory;
        private CancerStudyMetadata cancerStudymetadata;
        
        public FoundationStagingFileGenerator(Path xmlFile) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(xmlFile.toString()), "An XML file Path is required");
            try {
                this.baseStagingPath = resolveBaseStagingPath();
                this.cancerStudymetadata = this.resolveCancerStudyByName(xmlFile);
                this.stagingDirectory = this.resolveOutputDirectory(xmlFile);
                logger.info("Foundation staging files will be located in: " + this.stagingDirectory.toString());
                Files.copy(xmlFile, this.stagingDirectory.resolve(xmlFile.getFileName()));
            } catch (IOException ex) {
                logger.error("Exception copying source xml file " + xmlFile.toString());
                logger.error(ex.getMessage());
            }
            
        }

        /*
         * the XML file name without its extension is appended to the
         * base staging directory to get the output subdirectory
    
         */
        
        private Path resolveBaseStagingPath() throws IOException {
            Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(CommonNames.FOUNDATION_DATA_SOURCE_NAME);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("Cannot instantiate a proper DataSourcesMetadata object for Foundation data source");			
		}
		DataSourcesMetadata dsm = dataSourcesMetadata.iterator().next();
                logger.info("DataSourcesMetadata download directory " +dsm.getDownloadDirectory());
               return Paths.get(dsm.getDownloadDirectory());
        }
        
        private CancerStudyMetadata resolveCancerStudyByName (Path path){
            
            String cancerStudyName = this.resolveCancerStudyName(com.google.common.io.Files.getNameWithoutExtension(path.toString()));
            if(!Strings.isNullOrEmpty(cancerStudyName)) {
                 return config.getCancerStudyMetadataByName(cancerStudyName);
            }
            return null;
        }
        
        private String resolveCancerStudyName(String studySubstring){
            
            List<String> cancerStudyList = config.findCancerStudiesBySubstring(studySubstring.toLowerCase());
        if(null == cancerStudyList || cancerStudyList.isEmpty()){
            logger.info("There are no cancer study names associated with: " +studySubstring);
            return "";
        }
        if(cancerStudyList.size() > 1){
            logger.error("The cancer study name: " +studySubstring +" is not specific");
            for (String study : cancerStudyList){
                logger.info(study);
            }
        }
        
            return cancerStudyList.get(0);
        }
        
        private Path resolveOutputDirectory(Path path) throws IOException {
            String subdir = pathJoiner.join(baseStagingPath.toString(),                
                    this.cancerStudymetadata.getStudyPath());
            
            Path subPath = Paths.get(subdir);
            if (Files.isDirectory(subPath, LinkOption.NOFOLLOW_LINKS)) {
                org.apache.commons.io.FileUtils.deleteQuietly(subPath.toFile());
                logger.info("Deleted existing  sub directory: " + subPath.toString());
            }
            Files.createDirectory(subPath);
            logger.info("Created sub directory for cancer study  "
                    +this.cancerStudymetadata.getName() +" at "
                    + subPath.toString());
            return subPath;
            
        }
        
        public Optional<Path> getCNAReportPath() {
            return this.generateOutputPath(cnaReportName);
        }
        
        public Optional<Path> getMutationsReportPath() {
            return this.generateOutputPath(mutationsReportName);
        }
        
        public Optional<Path> getClinicalDataReportPath() {
            return this.generateOutputPath(clinicalReportName);
        }
        
        public Optional<Path> getFusionReportPath() {
            return this.generateOutputPath(fusionReportName);
        }

        /**
         * private method that returns an Optional containing a Path object to
         * the specified file Use of an Optional return object informs the
         * caller that the method may not return a usuable object
         *
         * @param fileName
         * @return
         */
        private Optional<Path> generateOutputPath(String aName) {
            String filename = pathJoiner.join(this.stagingDirectory.toString(), aName);
            try {
                
                Path outFile = Paths.get(filename);
                Files.deleteIfExists(outFile);
                return Optional.of(Files.createFile(outFile));
            } catch (IOException ex) {
                logger.error("Failed to create staging file " + filename);
                logger.error(ex.getMessage());
            }
            return Optional.absent();  // return an empty Optional
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

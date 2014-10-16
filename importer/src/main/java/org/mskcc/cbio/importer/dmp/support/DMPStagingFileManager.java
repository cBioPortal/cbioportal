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
package org.mskcc.cbio.importer.dmp.support;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Joiner;
import com.google.inject.internal.Preconditions;
import com.google.inject.internal.Sets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.DSYNC;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/*
 Resonsible for writing DMP data to MAF files
 */
public class DMPStagingFileManager {
    
    private final static Logger logger = Logger.getLogger(DMPStagingFileManager.class);
    private final Path stagingFilePath;
    private final Map<String, Path> filePathMap = Maps.newHashMap();
    private Set<String> processedSampleSet = Sets.newHashSet();
    private final Splitter tabSplitter = Splitter.on("\t");
    private final Splitter scSplitter = Splitter.on(";");
    private final Joiner scJoiner = Joiner.on(";");
    private final Path ttPath; // path for tumor type data

    public DMPStagingFileManager(Path aBasePath) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
        this.stagingFilePath = aBasePath;
        this.initFileMap();
        this.initProcessedSampleSet();
        // set Path for tumor type data
        this.ttPath = this.stagingFilePath.resolve("dmp_tumor_type.txt");
    }
    
    public Set<String> getProcessedSampleSet() {
        return this.processedSampleSet;
    }

    /*
     package level method to read in the current contents of the DMP tumor
     type data.
     returns an empty list if tumor type data does not exist
     */
    List<String> readDmpTumorTypeData() {
        try {
            return Files.readAllLines(this.ttPath, Charset.defaultCharset());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return Lists.newArrayList();
    }

    /*
     output of tumor type data
     tumor type data is handled in a read - delete - write mode
     */
    void writeTumorTypedata(List<String> lines) {
        try {
            // delete the existing tumor type file if one exists
            Files.deleteIfExists(this.ttPath);
            OpenOption[] options = new OpenOption[]{CREATE_NEW, WRITE};
            Files.write(this.ttPath, lines, Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    /*
    scan any exising staging files for previously persisted samples. 
    this set of processed sample ids will be used to detect sample updates
    in the input data
    */
    private void initProcessedSampleSet() {
        // process all the DMP staging files in the specified Path as tab-delimited text
        // sample ids are assumed to be in a specified  named column
        // add to the processed sample set
        for (Path path : this.filePathMap.values()) {
            try {
                final CSVParser parser = new CSVParser(new FileReader(path.toFile()), CSVFormat.TDF.withHeader());               
                Set<String> sampleSet = FluentIterable.from(parser).transform(new Function<CSVRecord,String>(){                 
                    @Override
                    public String apply(CSVRecord record) {                       
                        return record.get(DMPCommonNames.SAMPLE_ID_COLUMN_NAME);
                    }
                }).toSet();
                
                this.processedSampleSet.addAll(sampleSet);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
           
        }
        logger.info(this.processedSampleSet.size() + " unique DMP samples have been previously persisted");
    }
    
    private void initFileMap() {
       // this.filePathMap.put(DMPCommonNames.REPORT_TYPE_CNV, this.stagingFilePath.resolve("data_CNA.txt"));
       // this.filePathMap.put(DMPCommonNames.REPORT_TYPE_CNV_INTRAGENIC, this.stagingFilePath.resolve("data_CNA_intragenic.txt"));
        // both exonic and silent snps are written to the same file
        this.filePathMap.put(DMPCommonNames.REPORT_TYPE_MUTATIONS, this.stagingFilePath.resolve("data_mutations_extended.txt"));
      //  this.filePathMap.put(DMPCommonNames.REPORT_TYPE_SNP_SILENT, this.stagingFilePath.resolve("data_mutations_silent.txt"));
       // this.filePathMap.put(DMPCommonNames.REPORT_TYPE_SNP_EXONIC, this.stagingFilePath.resolve("data_mutations_exonic.txt"));

        // resolve Path to set of processed samples
        //create the staging files and write out column headings
        for (Map.Entry<String, Path> entry : this.filePathMap.entrySet()) {
            try {
                Path path = entry.getValue();

                // Files.deleteIfExists(path);
                if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createFile(path); // accept default file attributes
                    this.writeColumnHeaders(entry.getKey(), path);
                    logger.info("Staging file " + path.toString() + " created");
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    private void writeColumnHeaders(String reportType, Path path) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());
        writer.append(DmpUtils.getColumnNamesByReportType(reportType));
        writer.newLine();
        writer.flush();
    }
    
    public void appendMafDataToStagingFile(String reportType, List<String> mafData) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportType), "A DMP report type is required");
        Preconditions.checkArgument(this.filePathMap.containsKey(reportType),
                "Report type: " + reportType + " is not supported");
        Preconditions.checkArgument(null != mafData && !mafData.isEmpty(),
                "A valid List of MAF data is required");
        Path outPath = this.filePathMap.get(reportType);
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        // create the file if it doesn't exist, append to it if it does
        try {
            Files.write(outPath, mafData, Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }


    /*
     public method to transform a List of DMP sequence data to a List of Strings and
     output that List to the appropriate staging file based on the report type
     */
    public void appendDMPDataToStagingFile(String reportType, List aList, Function transformationFunction) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportType), "A DMP report type is required");
        Preconditions.checkArgument(this.filePathMap.containsKey(reportType),
                "Report type: " + reportType + " is not supported");
        Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of DMP data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Path outPath = this.filePathMap.get(reportType);
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        // create the file if it doesn't exist, append to it if it does
        try {
            Files.write(outPath, Lists.transform(aList, transformationFunction), Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /*
     remove records from the DMP staging files that have been deprecated
     refactored for revised staging file structure
     */
    /*
     Reader reader = new FileReader(this.tsvFile);
     // clear set of mutation ids
     this.mutationIdSet.clear();
     final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
     */
    public void removeDeprecatedSamplesFomStagingFiles(final Set<String> deprecatedSampleSet) {
        Preconditions.checkArgument(null != deprecatedSampleSet,
                "A set of deprecated samples is required");
        if (deprecatedSampleSet.size() > 0) {
            OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
          
           for (Map.Entry<String, Path> entry : this.filePathMap.entrySet()) {
                try {
                     Path path = entry.getValue();
                    // move staging file to a temporay file then write non-deprecated samples
                    // back to staging files
                    Path tempFilePath = Paths.get("/tmp/dmp/tempfile.txt");
                    Files.deleteIfExists(tempFilePath);
                    Files.move(path, tempFilePath);
                    logger.info(" processing " + tempFilePath.toString());
                    final CSVParser parser = new CSVParser(new FileReader(tempFilePath.toFile()), CSVFormat.TDF.withHeader());
                    // filter persisted sample ids that are also in the current data input
                    List<String> filteredSamples = FluentIterable.from(parser)
                            .filter(new Predicate<CSVRecord>() {
                                @Override
                                public boolean apply(CSVRecord record) {
                                    String sampleId = record.get(DMPCommonNames.SAMPLE_ID_COLUMN_NAME); // the column name is typically Tumor_Sample_Barcode
                                    if (!Strings.isNullOrEmpty(sampleId) && !deprecatedSampleSet.contains(sampleId)) {
                                        return true;
                                    }
                                    return false;
                                }
                            }).transform(new Function<CSVRecord, String>() {                           
                                @Override
                                public String apply(CSVRecord record) {
                                    return record.toString();
                                }
                            })
                            .toList();

                    // write the filtered data to the original DMP staging file
                    this.writeColumnHeaders(entry.getKey(), path);
                    Files.write(path, filteredSamples, Charset.defaultCharset(), options);
                    Files.delete(tempFilePath);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
                
            }
        }
    }
    
    public void removeDeprecatedSamplesFomStagingFilesOld(final Set<String> deprecatedSampleSet) {
        Preconditions.checkArgument(null != deprecatedSampleSet,
                "A set of deprecated samples is required");
        if (deprecatedSampleSet.size() > 0) {
            OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
            for (Path path : this.filePathMap.values()) {
                try {
                    // move staging file to a temporay file then write non-deprecated samples
                    // back to staging files
                    Path tempFilePath = Paths.get("/tmp/dmp/tempfile.txt");
                    Files.deleteIfExists(tempFilePath);
                    Files.move(path, tempFilePath);
                    logger.info(" processing " + tempFilePath.toString());
                    List<String> filteredSamples = FluentIterable.from(Files.readAllLines(tempFilePath, Charset.defaultCharset()))
                            .filter(new Predicate<String>() {
                                @Override
                                public boolean apply(String line) {
                                    String sampleId = tabSplitter.splitToList(line).get(0);
                                    // filter out lines whose sample id is contained in the deprecated set
                                    logger.info("filtering sample id " + sampleId);
                                    return !deprecatedSampleSet.contains(sampleId);
                                }
                            }).toList();
                    // write the filtered data to the original DMP staging file
                    Files.write(path, filteredSamples, Charset.defaultCharset(), options);
                    Files.delete(tempFilePath);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
                
            }
        }
    }
    
}

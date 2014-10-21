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
package org.mskcc.cbio.importer.persistence.staging;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.gdata.util.common.base.Joiner;
import com.google.inject.internal.Preconditions;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

/*
 an abstract class to support staging file operations common to most
 importer transformers
 */
public abstract class StagingFileManager {

    protected final Path stagingFilePath;
    protected final Map<String, Path> filePathMap = Maps.newHashMap();
    
    private final static Logger logger = Logger.getLogger(StagingFileManager.class);
    private final Path cnvPath;

    protected StagingFileManager(Path aBasePath) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
        this.stagingFilePath = aBasePath;
        // set Path for DMP CNV data
        this.cnvPath = this.stagingFilePath.resolve(StagingCommonNames.CNV_FILENAME);
    }

    /*
     public method to append a List of MAF data to the end of a designated file
     if the file does not exists, it will be created
     */
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

    public void transformImportDataToStagingFile(String reportType, List aList, Function transformationFunction) {
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
     public method to remove data for deprecated samples from registered staging files
     deprecated samples represent legacy samples that are being refreshed by
     new import data
     The result of this method is that the staging files will be replaced by files without 
     data for the specified samples
     */
    public void removeDeprecatedSamplesFomMAFStagingFiles(final String sampleIdColumnName, final Set<String> deprecatedSampleSet) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName), "The name of the sample id column is required");
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
                    List<String> columnHeadings =  Lists.newArrayList(parser.getHeaderMap().keySet());
                    // filter persisted sample ids that are also in the current data input
                    List<String> filteredSamples = FluentIterable.from(parser)
                            .filter(new Predicate<CSVRecord>() {
                                @Override
                                public boolean apply(CSVRecord record) {
                                    String sampleId = record.get(sampleIdColumnName); // the column name is typically Tumor_Sample_Barcode
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

                    // write the filtered data to the original MAF staging file
                    // column headings
                    Files.write(path,columnHeadings, Charset.defaultCharset(),options);
                    // data
                    Files.write(path, filteredSamples, Charset.defaultCharset(), options);
                    Files.delete(tempFilePath);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }

            }
        }
    }

  
    public Table<String, String, Double> initializeCnvTable() {

        Table<String, String, Double> cnvTable = HashBasedTable.create();
        // determine if there are persisted cnv data; if so read into Table data structure
        if (Files.exists(cnvPath, LinkOption.NOFOLLOW_LINKS)) {
            Reader reader = null;
            try {
                reader = new FileReader(this.cnvPath.toFile());
                final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
                Map<String, Integer> headerMap = parser.getHeaderMap();
                List<String> columnList = Lists.newArrayList();
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    columnList.add(entry.getKey());
                }
                // determine the column name used for the Hugo Symbol
                String geneColumnName = columnList.remove(0);
                // process each data row in table
                for (CSVRecord record : parser.getRecords()) {
                    String geneName = record.get(geneColumnName);
                    for (String sampleName : columnList) {
                        cnvTable.put(geneName, sampleName, Double.valueOf(record.get(sampleName)));
                    }

                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {

                }
            }

        }
        return cnvTable;
    }
    /*
     method to write out updated CNV data as TSV file
     rows = gene names
     columns = DMP smaple ids 
     values  = gene fold change
    
     since legacy entries may have been updated, previous file contents are overwritten
     */

    public void persistCnvTable(Table<String, String, Double> cnvTable) {
    }

}

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
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.internal.Preconditions;
import com.google.inject.internal.Sets;
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
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
/*
public class reposnsible for managing input/output operations with a 
collection of MAF staging files belonging to specified study
*/
public  class MafFileHandlerImpl  implements MafFileHandler{

    protected  Path stagingFilePath;
    
    private final static Logger logger = Logger.getLogger(MafFileHandlerImpl.class);
    
    
    public MafFileHandlerImpl(){}
    
    
    /*
    public interface method to register a Path to a MAF file for subsequent 
    staging file operations
    if the file does not exist, it will be created and a tab-delimited list of column headings
    will be written as the first line
    */
     @Override
    public void registerMafStagingFile(Path mafFilePath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != mafFilePath, "A Path object referencing the MAF file is required");
       if (!Files.exists(mafFilePath, LinkOption.NOFOLLOW_LINKS)) {
           Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                   "Column headings are required for the new MAF file: " +mafFilePath.toString());
            try {
                OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
                String line = StagingCommonNames.tabJoiner.join(columnHeadings) +"\n";
                Files.write(mafFilePath,
                        line.getBytes(),
                        options);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
           
       }
      this.stagingFilePath = mafFilePath;
    }

    public MafFileHandlerImpl(Path aBasePath,Map<String,String> fileMap) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
        Preconditions.checkArgument(null!= fileMap && !fileMap.isEmpty(), 
                "A Map containing report type(s) and file name(s) is required");
        this.stagingFilePath = aBasePath;
       
       
    }
    
   
    
     public Set<String> resolveProcessedSampleSet(final String sampleIdColumnName) {
        // process all the DMP staging files in the specified Path as tab-delimited text
        // sample ids are assumed to be in a specified  named column
        // add to the processed sample set
         Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName), 
                 "The sample id column nmae is required");
         Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the MAF staging file has not be specified");
         Set<String> processedSampleSet = Sets.newHashSet();
       
            try {
                final CSVParser parser = new CSVParser(new FileReader(this.stagingFilePath.toFile()), CSVFormat.TDF.withHeader());
                Set<String> sampleSet = FluentIterable.from(parser).transform(new Function<CSVRecord, String>() {
                    @Override
                    public String apply(CSVRecord record) {
                        return record.get(sampleIdColumnName);
                    }
                }).toSet();

                processedSampleSet.addAll(sampleSet);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }

      
      return processedSampleSet;
    }

    /*
     public method to append a List of MAF data to the end of a designated file
     if the file does not exists, it will be created
     */
    public void appendMafDataToStagingFile( List<String> mafData) {
        
        Preconditions.checkArgument(null != mafData && !mafData.isEmpty(),
                "A valid List of MAF data is required");
        Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the MAF staging file has not be specified");
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        // create the file if it doesn't exist, append to it if it does
        try {
            Files.write(this.stagingFilePath, mafData, Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    /*
     public method to transform a List of DMP sequence data to a List of Strings and
     output that List to the appropriate staging file based on the report type.
    If the staging file does not exist, it will be created
     */

    public void transformImportDataToStagingFile( List aList,
            Function transformationFunction) {
       
        Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of DMP data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the MAF staging file has not be specified");
        
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        // create the file if it doesn't exist, append to it if it does
        try {
            Files.write(this.stagingFilePath, Lists.transform(aList, transformationFunction), 
                    Charset.defaultCharset(), options);
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
        Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the MAF staging file has not be specified");
        if (deprecatedSampleSet.size() > 0) {
            OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
                try {               
                    // move staging file to a temporay file, filter out deprecated samples,
                    // then write non-deprecated samples
                    // back to staging files
                    Path tempFilePath = Paths.get("/tmp/dmp/tempfile.txt");
                    Files.deleteIfExists(tempFilePath);
                    Files.move(this.stagingFilePath, tempFilePath);
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
                    Files.write(this.stagingFilePath,columnHeadings, Charset.defaultCharset(),options);
                    // data
                    Files.write(this.stagingFilePath, filteredSamples, Charset.defaultCharset(), options);
                    Files.delete(tempFilePath);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
 
        }
    }

 
   
}

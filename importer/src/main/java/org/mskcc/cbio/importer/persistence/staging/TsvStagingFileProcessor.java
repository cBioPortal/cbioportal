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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;


import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Set;

import com.google.inject.internal.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;


public abstract class TsvStagingFileProcessor {
     protected  Path stagingFilePath;
    
    private final static Logger logger = Logger.getLogger(TsvStagingFileProcessor.class);
     
    protected void registerStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile) {

        /*
        option to delete an existing staging file to support replacement-mode staging
        operations
         */
        if(deleteFile){
            try {
                Files.deleteIfExists(stagingFilePath);
            } catch (IOException e) {
               logger.error(e.getMessage());
            }
        }

       // create the staging file if it doesn't exist and write out the column headers
       if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
           Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                   "Column headings are required for the new staging file: " 
                           +stagingFilePath.toString());
            try {
                OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
                String line = StagingCommonNames.tabJoiner.join(columnHeadings) +"\n";
                Files.write(stagingFilePath,
                        line.getBytes(),
                        options);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }       
       }
      this.stagingFilePath = stagingFilePath;
    }

    protected void transformImportDataToStagingFile(List aList,
            Function transformationFunction) {

        Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of staging data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Preconditions.checkState(null != this.stagingFilePath,
                "The requisite Path to the tsv staging file has not be specified");
        
        OpenOption[] options = new OpenOption[]{ APPEND, DSYNC};      
        try {
            Files.write(this.stagingFilePath, Lists.transform(aList, 
                    transformationFunction), 
                    Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    protected void removeDeprecatedSamplesFomTsvStagingFiles(final String sampleIdColumnName, final Set<String> deprecatedSampleSet) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName), "The name of the sample id column is required");
        Preconditions.checkArgument(null != deprecatedSampleSet,
                "A set of deprecated samples is required");
        Preconditions.checkState(null != this.stagingFilePath,
                "The requisite Path to the  staging file has not be specified");
        if (deprecatedSampleSet.size() > 0) {
            OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
            Path tempDir = null;
            Path tempFilePath = null;
            try {
                // move staging file to a temporary file, filter out deprecated samples,
                // then write non-deprecated samples
                // back to staging files
                //TODO: change this implementation

                //Path tempFilePath = Paths.get("/tmp/dmp/tempfile.txt");
                tempDir = Files.createTempDirectory("dmptemp");
                tempFilePath = Files.createTempFile(tempDir, ".txt" ,null);
                Files.deleteIfExists(tempFilePath);
                Files.move(this.stagingFilePath, tempFilePath);
                logger.info(" processing " + tempFilePath.toString());
                final CSVParser parser = new CSVParser(new FileReader(tempFilePath.toFile()),
                        CSVFormat.TDF.withHeader());
                String headings = StagingCommonNames.tabJoiner.join(parser.getHeaderMap().keySet());
                // filter persisted sample ids that are also in the current data input
                List<String> filteredSamples = FluentIterable.from(parser)
                        .filter(new Predicate<CSVRecord>() {
                            @Override
                            public boolean apply(CSVRecord record) {
                                String sampleId = record.get(sampleIdColumnName);
                                if (!Strings.isNullOrEmpty(sampleId) && !deprecatedSampleSet.contains(sampleId)) {
                                    return true;
                                }
                                return false;
                            }
                        }).transform(new Function<CSVRecord, String>() {
                            @Override
                            public String apply(CSVRecord record) {

                                return StagingCommonNames
                                        .tabJoiner
                                        .join(Lists.newArrayList(record.iterator()));
                            }
                        })
                        .toList();

                // write the filtered data to the original staging file
                // column headings
                Files.write(this.stagingFilePath,Lists.newArrayList(headings), Charset.defaultCharset(),options);
                // data
                Files.write(this.stagingFilePath, filteredSamples, Charset.defaultCharset(), options);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    Files.delete(tempFilePath);
                    Files.delete(tempDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /*
    method to scan a tsv staging file and return a list of processed sample ids
    based on a column  being identified as the sample id attribute
    this is to facilitate sample replacement processing
     */
    protected Set<String> resolveProcessedSampleSet(final String sampleIdColumnName) {
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

    public boolean isRegistered() { return null != this.stagingFilePath; }
}

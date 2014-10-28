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
import com.google.inject.internal.Preconditions;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;


public abstract class  TsvStagingFileHandler  {
     protected  Path stagingFilePath;
    
    private final static Logger logger = Logger.getLogger(TsvStagingFileHandler.class);
     
    protected void registerStagingFile(Path stagingFilePath, List<String> columnHeadings) {
         
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
                "A valid List of DMP data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Preconditions.checkState(null != this.stagingFilePath,
                "The requisite Path to the MAF staging file has not be specified");
        
        OpenOption[] options = new OpenOption[]{ APPEND, DSYNC};      
        try {
            Files.write(this.stagingFilePath, Lists.transform(aList, 
                    transformationFunction), 
                    Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    protected void removeDeprecatedSamplesFomMAFStagingFiles(final String sampleIdColumnName, final Set<String> deprecatedSampleSet) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName), "The name of the sample id column is required");
        Preconditions.checkArgument(null != deprecatedSampleSet,
                "A set of deprecated samples is required");
        Preconditions.checkState(null != this.stagingFilePath,
                "The requisite Path to the  staging file has not be specified");
        if (deprecatedSampleSet.size() > 0) {
            OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
            try {
                // move staging file to a temporary file, filter out deprecated samples,
                // then write non-deprecated samples
                // back to staging files
                Path tempFilePath = Paths.get("/tmp/dmp/tempfile.txt");
                Files.deleteIfExists(tempFilePath);
                Files.move(this.stagingFilePath, tempFilePath);
                logger.info(" processing " + tempFilePath.toString());
                final CSVParser parser = new CSVParser(new FileReader(tempFilePath.toFile()), CSVFormat.TDF.withHeader());
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

                // write the filtered data to the original MAF staging file
                // column headings
                Files.write(this.stagingFilePath,Lists.newArrayList(headings), Charset.defaultCharset(),options);
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

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
package org.mskcc.cbio.importer.persistence.staging.mutation;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.inject.internal.Preconditions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileProcessor;

/*
public class responsible for managing input/output operations with a
collection of MAF staging files belonging to specified study
*/
public  class MutationFileHandlerImpl extends TsvStagingFileProcessor implements TsvStagingFileHandler {

    private final static Logger logger = Logger.getLogger(MutationFileHandlerImpl.class);
     
    public MutationFileHandlerImpl(){}
   
    /*
    public interface method to register a Path to a MAF file for subsequent 
    staging file operations
    if the file does not exist, it will be created and a tab-delimited list of column headings
    will be written as the first line
    */
     @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != stagingFilePath, "A Path object referencing the MAF file is required");
       if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
           Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                   "Column headings are required for the new MAF file: " + stagingFilePath.toString());
           
       }
      super.registerStagingFile(stagingFilePath, columnHeadings,false);
    }

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile) {
        if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "Column headings are required for the new MAF file: " + stagingFilePath.toString());

        }
        super.registerStagingFile(stagingFilePath, columnHeadings,deleteFile);
    }


    public Set<String> resolveProcessedSampleSet(final String sampleIdColumnName) {
        // process all the DMP staging data in the specified Path as tab-delimited text
        // sample ids are assumed to be in a specified  named column
        // 
         Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName), 
                 "The sample id column nmae is required");
         Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the MAF staging file has not be specified");
        return super.resolveProcessedSampleSet(sampleIdColumnName);
    }

    /*
     public method to append a List of MAF data to the end of a designated file
     if the file does not exists, it will be created
     */
    @Override
    public void appendDataToTsvStagingFile(List<String> mafData) {
        
        Preconditions.checkArgument(null != mafData && !mafData.isEmpty(),
                "A valid List of MAF data is required");
        Preconditions.checkState(null != this.stagingFilePath, 
                 "The requisite Path to the MAF staging file has not be specified");
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

    @Override
    public void transformImportDataToTsvStagingFile(List aList,
                                                    Function transformationFunction) {

        super.transformImportDataToStagingFile(aList, transformationFunction);
    }

    /*
     public method to remove data for deprecated samples from registered staging files
     deprecated samples represent legacy samples that are being refreshed by
     new import data
     The result of this method is that the staging files will be replaced by files without 
     data for the specified samples
     */
    @Override
    public void removeDeprecatedSamplesFomTsvStagingFiles(final String sampleIdColumnName, final Set<String> deprecatedSampleSet) {
        super.removeDeprecatedSamplesFomTsvStagingFiles(sampleIdColumnName, deprecatedSampleSet);

        }
    }


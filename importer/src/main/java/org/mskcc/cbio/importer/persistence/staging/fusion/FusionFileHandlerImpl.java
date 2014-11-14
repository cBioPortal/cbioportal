package org.mskcc.cbio.importer.persistence.staging.fusion;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.inject.internal.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileProcessor;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/6/14.
 */
public class FusionFileHandlerImpl extends TsvStagingFileProcessor implements TsvStagingFileHandler {
    /*
    responsible for writing out fusion variant data to a tsv file
     */
    private final static Logger logger = Logger.getLogger(FusionFileHandlerImpl.class);

    public FusionFileHandlerImpl(){
        super();
    }

    /*
    public interface method to register a Path to a fusion file for subsequent
    staging file operations
    if the file does not exist, it will be created and a tab-delimited list of column headings
    will be written as the first line
    */

    public  void transformImportDataToTsvStagingFile(List aList,
                                                     Function transformationFunction) {

        com.google.common.base.Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of fusion staging  data is required");
        com.google.common.base.Preconditions.checkArgument(null != transformationFunction,
                "A fusion data transformation function is required");
        com.google.common.base.Preconditions.checkState(null != this.stagingFilePath,
                "The requisite Path to the fusion staging file has not be specified");

        super.transformImportDataToStagingFile(aList, transformationFunction);
    }

     /*
    public interface method to register a Path to a fusion file for subsequent
    staging file operations
    if the file does not exist, it will be created and a tab-delimited list of column headings
    will be written as the first line
    */

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != stagingFilePath, "A Path object referencing the fusion data file is required");
        if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "Column headings are required for the new fusion data file: " +stagingFilePath.toString());

        }
        // register this staging file but do not delete it if it exists
        super.registerStagingFile(stagingFilePath, columnHeadings,false);
    }

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile) {
        if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "Column headings are required for the new fusion data file: " +stagingFilePath.toString());

        }
        // register this staging file and allow the caller to determine if an existing file should be
        // deleted
        super.registerStagingFile(stagingFilePath, columnHeadings, deleteFile);
    }

    @Override
    public void appendDataToTsvStagingFile(List<String> mafData) {

    }

    /*
    provides support for import workflows where deprecated data must be removed from
    existing staging files before new data for the same sample ids can be appended
     */
    public void removeDeprecatedSamplesFomTsvStagingFiles(final String sampleIdColumnName,
                                                          final Set<String> deprecatedSampleSet) {
        super.removeDeprecatedSamplesFomTsvStagingFiles(sampleIdColumnName,deprecatedSampleSet);
    }

    /*
        public method to provide a set of sample ids in an existing fusion data file
        this is to support replacing deprecated data with newer data
         */
    public Set<String> resolveProcessedSampleSet(final String sampleIdColumnName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleIdColumnName),
                " The sample id column name is required");
        return super.resolveProcessedSampleSet(sampleIdColumnName);
    }


}

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

package org.mskcc.cbio.importer.persistence.staging.clinical;

import com.google.common.base.Function;
import com.google.inject.internal.Preconditions;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileProcessor;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;


/*
wrapper for TsvStagingFileHandler that provides context specific parameter
validation and messages
*/
public class ClinicalDataFileHandlerImpl extends TsvStagingFileProcessor
    implements ClinicalDataFileHandler {
     
    public ClinicalDataFileHandlerImpl() {} // default constructor

    /*
    Default Path registration method. Will delete an existing file at the Path location
     */
    @Override
    public void registerClinicalDataStagingFile(Path cdFilePath, List<String> columnHeadings) {
         Preconditions.checkArgument(null != cdFilePath, 
                 "A Path object referencing the clinical data  file is required");

           Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                   "Column headings are required for the new clinical data  file: " 
                           +cdFilePath.toString());

           super.registerStagingFile(cdFilePath, columnHeadings,true);
    }

    /*
    Alternative Path registration method that requires the caller to specify whether an existing file
    should be deleted or not. Useful for staging data are incrementally appended (e.g. DMP)
     */
    @Override
    public void registerClinicalDataStagingFile(Path cdFilePath, List<String> columnHeadings, boolean deleteFile) {
        Preconditions.checkArgument(null != cdFilePath,
                "A Path object referencing the clinical data  file is required");
        if (!Files.exists(cdFilePath, LinkOption.NOFOLLOW_LINKS)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "Column headings are required for the new clinical data  file: "
                            +cdFilePath.toString());
        }
        super.registerStagingFile(cdFilePath, columnHeadings,deleteFile);
    }

    @Override
    public void transformImportDataToStagingFile(List aList, Function transformationFunction) {
         Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of DMP data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Preconditions.checkState(null != this.stagingFilePath, 
                 "The requiste Path to the clinical data staging file has not be specified");
        super.transformImportDataToStagingFile(aList, transformationFunction);
    }


    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings) {

    }

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile) {

    }

    @Override
    public void appendDataToTsvStagingFile(List<String> mafData) {

    }

    @Override
    public void transformImportDataToTsvStagingFile(List aList, Function transformationFunction) {
        super.transformImportDataToStagingFile(aList, transformationFunction);
    }
}

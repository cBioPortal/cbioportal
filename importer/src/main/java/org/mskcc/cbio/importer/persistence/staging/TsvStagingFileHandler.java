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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;


public interface TsvStagingFileHandler {
    
    /*
    register the MAF file for staging data with handler. If the file does not
    exist, create it and write out the column headings as tsv
    */
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings);

    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile);

    public void appendDataToTsvStagingFile(List<String> mafData);

    public Set<String> resolveProcessedSampleSet(final String sampleIdColumnName);

    /*
    remove records from the DMP staging files that have been deprecated
     */
     public void removeDeprecatedSamplesFomTsvStagingFiles(final String sampleIdColumnName,
                                                           final Set<String> deprecatedSampleSet) ;

    /*
    public method to transform a List of sequence data to a List of Strings and
    output that List to the appropriate staging file based on the report type
     */
    public void transformImportDataToTsvStagingFile(List aList,
                                                    Function transformationFunction);
}

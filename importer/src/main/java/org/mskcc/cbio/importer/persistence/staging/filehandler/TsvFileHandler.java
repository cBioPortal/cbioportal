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
package org.mskcc.cbio.importer.persistence.staging.filehandler;

import com.google.common.base.Function;
import com.google.common.collect.Table;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;


public interface TsvFileHandler {
    


    /*
    method provides a set of existing sample ids in the previously registered staging file
    user specifies which column in the tsv file is the sample id
    throws an illegal state exception if a staging file has not been registered
     */
    public Set<String> resolveProcessedSampleSet(final String sampleIdColumnName);

    /*
    remove records from the registered staging file that have been deprecated
     */
     public void removeDeprecatedSamplesFomTsvStagingFiles(final String sampleIdColumnName,
                                                           final Set<String> deprecatedSampleSet) ;

    /*
    public method to transform a List of sequence data to a List of Strings and
    output that List to the registered staging file
     */
    public void transformImportDataToTsvStagingFile(List aList,
                                                    Function transformationFunction);

    /*
    public methos to verify that a staging file has been associated with the file handler
     */
    public boolean isRegistered();

    public Table<String, String, String> initializeCnvTable();
    /*
     method to write out updated CNV data as TSV file
     rows = gene names
     columns = DMP smaple ids
     values  = gene fold change
     since legacy entries may have been updated, previous file contents are overwritten
     */

    public void persistCnvTable(Table<String, String, String> cnvTable);

}

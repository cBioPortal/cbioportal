/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
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
*/

// package
package org.mskcc.cbio.importer.dao;

// imports
import org.mskcc.cbio.importer.model.ImportDataRecord;

import java.util.List;

/**
 * Interface  used to input/output ImportData into database.
 */
public interface ImportDataRecordDAO {

	/**
	 * Persists the given ImportDataRecord object.
	 *
	 * @param importDataRecord ImportDataRecord
	 */
	void importDataRecord(ImportDataRecord importDataRecord);

    /**
     * Functon to retrieve all ImportDataRecord.
	 *
	 * @return List<ImportDataRecord>
     */
    List<ImportDataRecord> getImportDataRecords();

    /**
     * Functon to retrieve ImportDataRecord via tumor type, data type, and center.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param center String
	 * @param runDate String
	 * @return List<ImportDataRecord>
     */
    List<ImportDataRecord> getImportDataRecordByTumorTypeAndDatatypeAndCenterAndRunDate(String tumorType, String datatype, String center, String runDate);

    /**
     * Functon to retrieve ImportDataRecord via tumor type and datatype and data filename
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param dataFilename String
	 * @param runDate String
	 * @return ImportDataRecord
     */
    ImportDataRecord getImportDataRecordByTumorAndDatatypeAndDataFilenameAndRunDate(String tumorType, String datatype, String dataFilename, String runDate);

	/**
	 * Function to delete records with the given dataSource.
	 *
	 * @param dataSource String
	 */
	void deleteByDataSource(String dataSource);
}

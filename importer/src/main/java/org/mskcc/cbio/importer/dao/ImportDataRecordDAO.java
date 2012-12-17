/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.dao;

// imports
import org.mskcc.cbio.importer.model.ImportDataRecord;
import java.util.Collection;

/**
 * Interface  used to input/output ImportData into database.
 */
public interface ImportDataRecordDAO {

	/**
	 * Persists the given ImportDataRecord object.
	 *
	 * @param importDataRecord ImportDataRecord
	 */
	void importDataRecord(final ImportDataRecord importDataRecord);

    /**
     * Functon to retrieve all ImportDataRecord.
	 *
	 * @return Collection<ImportDataRecord>
     */
    Collection<ImportDataRecord> getImportDataRecords();

    /**
     * Functon to retrieve ImportDataRecord via tumor type and data type.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @return Collection<ImportDataRecord>
     */
    Collection<ImportDataRecord> getImportDataRecordByTumorAndDatatype(final String tumorType, final String datatype);

    /**
     * Functon to retrieve ImportDataRecord via tumor type, data type, and data source.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param dataSource String
	 * @return Collection<ImportDataRecord>
     */
    Collection<ImportDataRecord> getImportDataRecordByTumorAndDatatypeAndDataSource(final String tumorType, final String datatype, final String dataSource);

    /**
     * Functon to retrieve ImportDataRecord via tumor type and data type and data filename
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param dataFilename String
	 * @return ImportDataRecord
     */
    ImportDataRecord getImportDataRecordByTumorAndDatatypeAndDataFilename(final String tumorType, final String datatype, final String dataFilename);

	/**
	 * Function to delete records with the given dataSource.
	 *
	 * @param dataSource String
	 */
	void deleteByDataSource(final String dataSource);
}

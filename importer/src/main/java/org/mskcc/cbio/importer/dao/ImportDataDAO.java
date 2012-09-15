// package
package org.mskcc.cbio.importer.dao;

// imports
import org.mskcc.cbio.importer.model.ImportData;
import java.util.Collection;

/**
 * Interface  used to input/output ImportData into database.
 */
public interface ImportDataDAO {

	/**
	 * Persists the given ImportData object.
	 *
	 * @param importData ImportData
	 */
	void importData(final ImportData importData);

    /**
     * Functon to retrieve all ImportData.
	 *
	 * @return Collection<ImportData>
     */
    Collection<ImportData> getImportData();

    /**
     * Functon to retrieve ImportData via tumor type and data type.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @return ImportData
     */
    ImportData getImportDataByTumorAndDatatype(final String tumorType, final String datatype);
}

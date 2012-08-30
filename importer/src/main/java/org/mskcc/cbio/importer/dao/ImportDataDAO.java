// package
package org.mskcc.cbio.importer.dao;

// imports
import org.mskcc.cbio.importer.model.ImportData;

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
     * Functon to retrieve ImportData via cancer type and data type.
	 *
	 * @param cancerType String
	 * @param dataType String
	 * @return ImportData
     */
    ImportData getImportDataByCancerAndDatatype(final String cancerType, final String datatype);
}

// package
package org.mskcc.cbio.importer;

// imports

/**
 * Interface used to import portal data.
 */
public interface Importer {

	/**
	 * Imports data into the given database for use in the given portal.
	 *
	 * @param database String
     * @param portal String
	 * @throws Exception
	 */
	void importData(final String database, final String portal) throws Exception;
}
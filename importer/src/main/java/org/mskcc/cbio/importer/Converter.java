// package
package org.mskcc.cbio.importer;

// imports

/**
 * Interface used to convert portal data.
 */
public interface Converter {

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
	void convertData(final String portal) throws Exception;

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
	void generateCaseLists(final String portal) throws Exception;
}
// package
package org.mskcc.cbio.importer;

// imports

/**
 * Interface used to retrieve portal data.
 */
public interface Fetcher {

	/**
	 * Fetchers data from an external datasource.
	 *
	 * @throws Exception
	 */
	void fetch() throws Exception;
}
// package
package org.mskcc.cbio.firehose;

// imports

/**
 * Interface used to retrieve firehose data.
 */
public interface Fetcher {

	/**
	 * Fetchers data from the Broad.
	 *
	 * @throws Exception
	 */
	void fetch() throws Exception;
}
// package
package org.mskcc.cbio.firehose;

// imports
import java.io.IOException;
import java.text.ParseException;

/**
 * Interface used to retrieve firehose data.
 */
public interface Fetcher {

	/**
	 * Fetchers data from the Broad.
	 *
	 * @throws ParseException - improper date format
	 * @throws IOException - reading firehose_get output
	 * @throws InterruptedException - executing process via runtime
	 */
	void fetch() throws ParseException, IOException, InterruptedException;
}
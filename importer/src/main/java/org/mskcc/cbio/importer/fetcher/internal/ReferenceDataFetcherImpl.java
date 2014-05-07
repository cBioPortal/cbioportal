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
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.util.Shell;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.Arrays;

/**
 * Class which implements the fetcher interface.
 */
class ReferenceDataFetcherImpl implements Fetcher {

	// our logger
	private static Log LOG = LogFactory.getLog(ReferenceDataFetcherImpl.class);

	// ref to file utils
	protected FileUtils fileUtils;

	/**
	 * Constructor.
     *
	 * Takes a FileUtils reference.
     *
	 * @param fileUtils FileUtils
	 */
	public ReferenceDataFetcherImpl(FileUtils fileUtils) {

		// set members
		this.fileUtils = fileUtils;
	}

	/**
	 * Fetchers genomic data from an external datasource and
	 * places in database for processing.
	 *
	 * @param dataSource String
	 * @param desiredRunDate String
	 * @throws Exception
	 */
	@Override
	public void fetch(String dataSource, String desiredRunDate) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Fetchers reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {

		String fetcherName = referenceMetadata.getFetcherName();

		if (fetcherName.length() == 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetchReferenceData(), no fetcher name provided, exiting...");
			}
			return;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), fetcherName: " + fetcherName);
		}

		Object[] args = { fileUtils };
		if (Shell.exec(referenceMetadata, this, args, ".")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetchReferenceData(), successfully executed fetcher.");
			}
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), failure executing importer.");
		}
	}
}

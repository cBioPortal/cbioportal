// package
package org.mskcc.cbio.importer.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Importer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which implements the Importer interface.
 */
final class ImporterImpl implements Importer {

	// our logger
	private static final Log LOG = LogFactory.getLog(ImporterImpl.class);

	// ref to configuration
	private Config config;

	/**
	 * Constructor.
     *
     * Takes a Config reference.
     *
     * @param config Config
	 */
	public ImporterImpl(final Config config) {

		// set members
		this.config = config;
	}

	/**
	 * Imports data into the given database.
	 *
	 * @param database String
	 * @throws Exception
	 */
	@Override
	public void importData(final String database) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData()");
		}
	}
}

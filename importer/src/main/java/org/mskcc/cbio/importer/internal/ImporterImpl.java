// package
package org.mskcc.cbio.importer.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.FileUtils;

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

	// ref to file utils
	private FileUtils fileUtils;

	/**
	 * Constructor.
     *
     * Takes a Config & FileUtils reference.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 */
	public ImporterImpl(final Config config, final FileUtils fileUtils) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
	}

	/**
	 * Imports data into the given database for use in the given portal.
	 *
	 * @param database String
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void importData(final String database, final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData()");
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}
	}
}

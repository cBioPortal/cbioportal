/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.util.Shell;

import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.compress.compressors.gzip.GzipUtils;

import java.util.Arrays;

/**
 * Class which implements the Importer interface, specifically for importing human gene info from BridgeDB.
 */
public final class BridgeDBHumanGeneImporterImpl implements Importer {

	// our logger
	private static final Log LOG = LogFactory.getLog(BridgeDBHumanGeneImporterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	/**
	 * Constructor.
     *
     * Takes a Config, FileUtils, & DatabaseUtils reference.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 */
	public BridgeDBHumanGeneImporterImpl(final Config config, final FileUtils fileUtils, final DatabaseUtils databaseUtils) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
	}

	/**
	 * Imports data for use in the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void importData(final String portal) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Imports the given reference data.
	 *
     * @param referenceMetadata String
	 * @throws Exception
	 */
	@Override
	public void importReferenceData(final ReferenceMetadata referenceMetadata) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), referenceMetadata: " + referenceMetadata.getReferenceType());
		}

		// first create (and clobber an existing) db
		databaseUtils.createDatabase(databaseUtils.getGeneInformationDatabaseName(), false);

		String referenceFile = referenceMetadata.getReferenceFile();
		if (GzipUtils.isCompressedFilename(referenceFile)) {
			referenceFile = GzipUtils.getUncompressedFilename(referenceFile);
		}

		// use mysql to import the .sql file
		String[] command = new String[] {"mysql",
										 "--user=" + databaseUtils.getDatabaseUser(),
										 "--password=" + databaseUtils.getDatabasePassword(),
										 databaseUtils.getGeneInformationDatabaseName(),
										 "-e",
										 "source " + referenceFile};
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + Arrays.asList(command));
			LOG.info("this may take a while...");
		}

		if (Shell.exec(Arrays.asList(command), ".")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importReferenceData complete.");
			}
		}
	}
}

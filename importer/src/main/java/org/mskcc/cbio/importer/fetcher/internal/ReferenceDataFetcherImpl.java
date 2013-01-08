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
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

		// sanity check
		if (referenceMetadata.getReferenceFileSource() == null ||
			referenceMetadata.getReferenceFileSource().getFile().length() == 0) {
			throw new IllegalArgumentException("referenceMetadata.getReferenceFileSource() must not be null, aborting");
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), fetching reference file: " + referenceMetadata.getReferenceFileSource().getFile());
			LOG.info("fetchReferenceData(), destination: " + referenceMetadata.getReferenceFile().getFile());
		}

		fileUtils.downloadFile(referenceMetadata.getReferenceFileSource().getFile(),
							   referenceMetadata.getReferenceFile().getFile());
	}
}

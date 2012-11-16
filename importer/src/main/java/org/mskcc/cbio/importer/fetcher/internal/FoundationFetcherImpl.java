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
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.dao.ImportDataDAO;

import org.tempuri.*;
import org.foundation.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

/**
 * Class which implements the fetcher interface.
 */
final class FoundationFetcherImpl implements Fetcher {

	// our logger
	private static final Log LOG = LogFactory.getLog(FoundationFetcherImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to import data
	private ImportDataDAO importDataDAO;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// download directories
	private DataSourceMetadata dataSourceMetadata;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 * @param importDataDAO ImportDataDAO;
	 */
	public FoundationFetcherImpl(final Config config, final FileUtils fileUtils,
								 final DatabaseUtils databaseUtils, final ImportDataDAO importDataDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataDAO = importDataDAO;
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
	public void fetch(final String dataSource, final String desiredRunDate) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
		}

		// get our DataSourceMetadata object
		Collection<DataSourceMetadata> dataSources = config.getDataSourceMetadata(dataSource);
		if (!dataSources.isEmpty()) {
			this.dataSourceMetadata = dataSources.iterator().next();
		}
		// sanity check
		if (this.dataSourceMetadata == null) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourceMetadata object.");
		}

		// create a case info service stub
		CaseInfoServiceStub foundationServices = new CaseInfoServiceStub();

		// get all the cases
		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), calling GetAll...");
		}
		GetAllDocument request = GetAllDocument.Factory.newInstance();
		GetAllResponseDocument response = foundationServices.GetAll(request);
		if (response != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetch(), getAllResponse().getAllResult: " + response.getGetAllResponse().getGetAllResult());
			}
		}


		// outta here
		//foundationServices.Close();
	}

	/**
	 * Fetchers reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(final ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}
}

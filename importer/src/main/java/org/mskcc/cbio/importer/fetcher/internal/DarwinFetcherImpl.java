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

package org.mskcc.cbio.importer.fetcher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.dao.internal.DarwinDAO;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import java.io.File;
import java.util.Collection;

/**
 *  Fetcher for Darwin data.
 */
public class DarwinFetcherImpl implements Fetcher
{
	// our logger
	private static final Log LOG = LogFactory.getLog(DarwinFetcherImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to dao
	private DarwinDAO dao;

	// download directories
	private DataSourcesMetadata dataSourceMetadata;

	/**
	 * Constructor.
	 *
	 * @param config Config
	 * @param fileUtils FileUtils
	 * @param dao DarwinDAO
	 */
	public DarwinFetcherImpl(Config config, FileUtils fileUtils, DarwinDAO dao)
	{
		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.dao = dao;
	}

	@Override
	public void fetch(String dataSource, String desiredRunDate) throws Exception
	{
		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
		}

		// get our DataSourcesMetadata object
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");
		}
		this.dataSourceMetadata = dataSourcesMetadata.iterator().next();

		// clinical data content
		StringBuilder dataClinicalContent = new StringBuilder();

		// TODO use DAO to fetch data
		System.out.println("TODO initiate dao and fetch data");
		this.addClinicalData(this.dao, dataClinicalContent);

		// TODO write contents to output directory
		this.generateClinicalDataFile(dataClinicalContent);
	}

	protected void addClinicalData(DarwinDAO dao, StringBuilder content)
	{
		content.append("TODO\tTODO\n");
		dao.getAllClinicalData();
	}

	protected File generateClinicalDataFile(StringBuilder content) throws Exception
	{
		// TODO use constants in FileUtils
		String header = "AGE\t" +
		                "GENDER\n";

		File clinicalFile = fileUtils.createFileWithContents(
				dataSourceMetadata.getDownloadDirectory() + File.separator +
					DatatypeMetadata.CLINICAL_STAGING_FILENAME,
				header + content.toString());

		return clinicalFile;
	}

	/**
	 * Fetches reference data from an external datasource.
	 *
	 * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}
}

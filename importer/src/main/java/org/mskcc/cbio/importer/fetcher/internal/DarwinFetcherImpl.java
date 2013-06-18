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

	// ref to import data
	private ImportDataRecordDAO importDataRecordDAO;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// download directories
	private DataSourcesMetadata dataSourceMetadata;

	/**
	 * Constructor.
	 *
	 * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 * @param importDataRecordDAO ImportDataRecordDAO;
	 */
	public DarwinFetcherImpl(Config config, FileUtils fileUtils,
			DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
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
		this.addClinicalData(null, dataClinicalContent);

		// TODO write contents to output directory
		this.generateClinicalDataFile(dataClinicalContent);
	}

	protected void addClinicalData(DarwinDAO dao, StringBuilder content)
	{
		content.append("TODO\tTODO\n");
	}

	protected File generateClinicalDataFile(StringBuilder content) throws Exception
	{
		String header = "AGE\t" +
		                "GENDER\n";

		File clinicalFile = fileUtils.createFileWithContents(
				dataSourceMetadata.getDownloadDirectory() + File.separator + "data_clinical.txt",
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

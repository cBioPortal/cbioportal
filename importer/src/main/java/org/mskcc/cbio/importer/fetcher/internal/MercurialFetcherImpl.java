/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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

import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.util.Shell;
import org.mskcc.cbio.importer.mercurial.*;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * Class which implements the fetcher interface.
 */
public class MercurialFetcherImpl extends FetcherBaseImpl implements Fetcher
{
	private static Log LOG = LogFactory.getLog(MercurialFetcherImpl.class);

	private Config config;
	private FileUtils fileUtils;
	private ImportDataRecordDAO importDataRecordDAO;
	private DatabaseUtils databaseUtils;
	private MercurialService mercurialService;

	public MercurialFetcherImpl(Config config, FileUtils fileUtils,
								DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO,
								MercurialService mercurialService)
	{
		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
		this.mercurialService = mercurialService;
	}

	@Override
	public boolean fetch(String dataSource, String desiredRunDate) throws Exception
	{
		logMessage(LOG, "fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);

		DataSourcesMetadata dataSourceMetadata = getDataSourceMetadata(dataSource);
		boolean updatesAvailable = mercurialService.updatesAvailable(dataSourceMetadata.getDownloadDirectory());
		if (updatesAvailable) {
			logMessage(LOG, "fetch(), updates available, pulling from repository.");
			updateStudiesWorksheet(dataSourceMetadata.getDownloadDirectory(),
			                       mercurialService.pullUpdate(dataSourceMetadata.getDownloadDirectory()));
			return true;
		}
		else {
			logMessage(LOG, "fetch(), we have the latest dataset, nothing more to do.");
			return false;
		}
	}

	private DataSourcesMetadata getDataSourceMetadata(String dataSource)
	{
		// get our DataSourcesMetadata object
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");
		}
		return dataSourcesMetadata.iterator().next();
	}

	@Override
	public boolean fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}

	private List<String> updateStudiesWorksheet(String downloadDirectory, List<String> studiesUpdated)
	{
		ArrayList<String> cancerStudiesUpdated = new ArrayList<String>(); 
		Map<String,String> propertyMap = new HashMap<String,String>();
		for (String cancerStudy : studiesUpdated) {
			CancerStudyMetadata cancerStudyMetadata = getCancerStudyMetadata(downloadDirectory, cancerStudy);
			if (cancerStudyMetadata == null) {
				continue;
			}
			if (cancerStudyMetadataExists(cancerStudy)) {
				propertyMap.clear();
				propertyMap.put(CancerStudyMetadata.UPDATE_AVAILABLE_COLUMN_KEY, "true");
				// clear import (if requires validation)
				if (cancerStudyMetadata.requiresValidation()) {
					propertyMap.put(CancerStudyMetadata.IMPORT_COLUMN_KEY, "false");
				}
				config.updateCancerStudyAttributes(cancerStudy, propertyMap);
				logMessage(LOG, "fetch(), the following study has been updated: " + cancerStudy);
			}
			else {
				config.insertCancerStudyMetadata(cancerStudyMetadata);
				logMessage(LOG, "fetch(), the following study has been created: " + cancerStudy);
			}
			cancerStudiesUpdated.add(cancerStudy);
		}
		return cancerStudiesUpdated;
	}

	private CancerStudyMetadata getCancerStudyMetadata(String downloadDirectory, String cancerStudy)
	{
		CancerStudyMetadata cancerStudyMetadata = config.getCancerStudyMetadataByName(cancerStudy);
		if (cancerStudyMetadata == null) {
			// cancer study is unknown, create an entry
			cancerStudyMetadata = fileUtils.createCancerStudyMetadataFromMetaStudyFile(downloadDirectory, cancerStudy);
		}
		return cancerStudyMetadata;
	}

	private boolean cancerStudyMetadataExists(String cancerStudy)
	{
		return (config.getCancerStudyMetadataByName(cancerStudy) != null);
	}
}


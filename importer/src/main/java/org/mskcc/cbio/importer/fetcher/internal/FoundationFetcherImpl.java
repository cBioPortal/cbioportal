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
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;

//import org.foundation.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which implements the fetcher interface.
 */
class FoundationFetcherImpl implements Fetcher {

	// our logger
	private static final Log LOG = LogFactory.getLog(FoundationFetcherImpl.class);

	// foundation data file extension
	private static final String FOUNDATION_FILE_EXTENSION = ".xml";

	// not all fields in ImportDataRecord will be used
	private static final String UNUSED_IMPORT_DATA_FIELD = "NA";

	// regex used when getting a case list from the broad
    private static final Pattern FOUNDATION_CASE_LIST_RECORD = 
		Pattern.compile("^\\s*\\<Case fmiCase=\\\"\\w*\\\" case=\\\"(\\w*-\\w*)\\\" \\/\\>$");

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
	public FoundationFetcherImpl(Config config, FileUtils fileUtils,
								 DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
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

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
		}

		// get our DataSourcesMetadata object
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");			
		}
		this.dataSourceMetadata = dataSourcesMetadata.iterator().next();

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), creating CaseInfoService endpoint.");
		}
//		CaseInfoService caseInfoService = new CaseInfoService();
//		ICaseInfoService foundationService = caseInfoService.getICaseInfoService();
//
//		if (LOG.isInfoEnabled()) {
//			LOG.info("fetch(), fetching case list.");
//		}
//		String[] caseList = foundationService.getCaseList().split("\n");
//		for (String caseListRecord : caseList) {
//			Matcher matcher = FOUNDATION_CASE_LIST_RECORD.matcher(caseListRecord);
//			if (matcher.find()) {
//				String caseID = matcher.group(1);
//				if (LOG.isInfoEnabled()) {
//					LOG.info("fetch(), fetching case : " + caseID);
//				}
//				try {
//					String caseRecord = foundationService.getCase(caseID);
//					File caseFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
//																	 File.pathSeparator + 
//																	 caseID + FOUNDATION_FILE_EXTENSION, caseRecord);
//					if (LOG.isInfoEnabled()) {
//						LOG.info("fetch(), successfully fetched data for case: " + caseID + ", persisting...");
//					}
//					ImportDataRecord importDataRecord = new ImportDataRecord(dataSource, dataSource, UNUSED_IMPORT_DATA_FIELD,
//                                                                             UNUSED_IMPORT_DATA_FIELD, UNUSED_IMPORT_DATA_FIELD,
//                                                                             caseFile.getCanonicalPath(), UNUSED_IMPORT_DATA_FIELD,
//                                                                             caseID + FOUNDATION_FILE_EXTENSION);
//					importDataRecordDAO.importDataRecord(importDataRecord);
//				}
//				catch (ServerSOAPFaultException e) {
//					// we get here if record does not exist on server side (yet)
//					if (LOG.isInfoEnabled()) {
//						LOG.info("fetch(), Cannot fetch case record for case: " + caseID);
//					}
//				}
//			}
//		}
	}

	/**
	 * Fetchers reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}
}

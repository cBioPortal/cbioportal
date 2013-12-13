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
import org.mskcc.cbio.importer.Admin;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;

import org.mskcc.cbio.portal.web_api.ConnectionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.*;

import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.io.File;
import java.io.InputStream;

class BiotabFetcherImpl extends FetcherBaseImpl implements Fetcher
{
    private static final String TUMOR_TYPE_REGEX = "<TUMOR_TYPE>";
	private static final Log LOG = LogFactory.getLog(BiotabFetcherImpl.class);

	private Config config;
	private FileUtils fileUtils;
	private ImportDataRecordDAO importDataRecordDAO;
	private DatabaseUtils databaseUtils;
	private DataSourcesMetadata dataSourceMetadata;

	private String tcgaClinicalURL;
	@Value("${tcga.clinical.url}")
	public void setTCGAClinicalURL(String tcgaClinicalURL) { this.tcgaClinicalURL = tcgaClinicalURL; }
	public String getTCGAClinicalURL() { return this.tcgaClinicalURL; }

	private String tcgaClinicalFilename;
	@Value("${tcga.clinical.filename}")
	public void setTCGAClinicalFilename(String tcgaClinicalFilename) { this.tcgaClinicalFilename = tcgaClinicalFilename; }
	public String getTCGAClinicalFilename() { return this.tcgaClinicalFilename; }

	public BiotabFetcherImpl(Config config, FileUtils fileUtils,
                             DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO)
    {

		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
	}

	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception
    {
		throw new UnsupportedOperationException();
	}

    @Override
	public void fetch(String dataSource, String desiredRunDate) throws Exception
    {

		logMessage(LOG, "fetch(), dateSource" + dataSource);
        initDataSourceMetadata(dataSource);
        fetchData();
        logMessage(LOG, "fetch(), complete.");
	}

    private void initDataSourceMetadata(String dataSource) throws Exception
    {
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");			
		}
		this.dataSourceMetadata = dataSourcesMetadata.iterator().next();
    }

    private void fetchData() throws Exception
    {
        for (String tumorType : config.getTumorTypesToDownload()) {
            saveClinicalForTumorType(tumorType);
        }
    }

    private void saveClinicalForTumorType(String tumorType)
    {
        HttpClient client = getHttpClient();
        GetMethod method = new GetMethod(getSourceURL(tumorType));

        try {
            if (client.executeMethod(method) == HttpStatus.SC_OK) {
                saveClinicalData(tumorType, method.getResponseBodyAsStream());
            }
        }
        catch (Exception e) {}
        finally {
            method.releaseConnection();
        }
    }

    private HttpClient getHttpClient()
    {
        MultiThreadedHttpConnectionManager connectionManager =
            ConnectionManager.getConnectionManager();
        return new HttpClient(connectionManager);
    }

    private String getSourceURL(String tumorType)
    {
        return (tcgaClinicalURL.replace(TUMOR_TYPE_REGEX, tumorType) +
                tcgaClinicalFilename.replace(TUMOR_TYPE_REGEX, tumorType));
    }

    private void saveClinicalData(String tumorType, InputStream is) throws Exception
    {
        File clinicalDataFile =  fileUtils.createFileFromStream(getDestinationFilename(tumorType), is);
        createImportDataRecord(tumorType, clinicalDataFile);
    }

    private String getDestinationFilename(String tumorType) throws Exception
    {
        return (getDownloadDirectory() +
                File.separator +
                tumorType +
                File.separator +
                tcgaClinicalFilename.replace(TUMOR_TYPE_REGEX, tumorType));
    }

    private String getDownloadDirectory() throws Exception
    {
        File downloadDirectory = new File(dataSourceMetadata.getDownloadDirectory());
        if (!downloadDirectory.exists()) {
            fileUtils.makeDirectory(downloadDirectory);
        }

        return downloadDirectory.getCanonicalPath();
    }


    
    private void createImportDataRecord(String tumorType, File clinicalDataFile) throws Exception
    {
        String computedDigest = fileUtils.getMD5Digest(clinicalDataFile);
        for (DatatypeMetadata datatype : config.getFileDatatype(dataSourceMetadata, tcgaClinicalFilename)) {
            if (!datatype.isDownloaded()) continue;
            for (String archivedFile : datatype.getTCGAArchivedFiles(tcgaClinicalFilename)) {
                ImportDataRecord importDataRecord = new ImportDataRecord(dataSourceMetadata.getDataSource(),
                                                                         "tcga",
                                                                         tumorType, tumorType,
                                                                         datatype.getDatatype(),
                                                                         Fetcher.LATEST_RUN_INDICATOR,
                                                                         clinicalDataFile.getCanonicalPath(),
                                                                         computedDigest, archivedFile);
                importDataRecordDAO.importDataRecord(importDataRecord);
            }
        }
    }
}

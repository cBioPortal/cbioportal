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

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.SimpleMailMessage;

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

	//@Autowired
	//JavaMailSender mailSender;

	//@Autowired
	//SimpleMailMessage triageUpdateMessage;

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
	public void fetch(String dataSource, String desiredRunDate, boolean sendNotification) throws Exception
	{
		logMessage(LOG, "fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);

		DataSourcesMetadata dataSourceMetadata = getDataSourceMetadata(dataSource);
		boolean updatesAvailable = mercurialService.updatesAvailable(dataSourceMetadata.getDownloadDirectory());
		if (updatesAvailable) {
			logMessage(LOG, "fetch(), updates available, pulling from repository.");
			List<String> cancerStudiesUpdated = updateStudiesWorksheet(dataSourceMetadata,
			                                                           mercurialService.pullUpdate(dataSourceMetadata.getDownloadDirectory()));
			if (sendNotification) {
				sendNotification(cancerStudiesUpdated);
			}
		}
		else {
			logMessage(LOG, "fetch(), we have the latest dataset, nothing more to do.");
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
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}

	private List<String> updateStudiesWorksheet(DataSourcesMetadata dataSourceMetadata, List<String> studiesUpdated)
	{
		ArrayList<String> cancerStudiesUpdated = new ArrayList<String>(); 
		for (String cancerStudy : studiesUpdated) {
			CancerStudyMetadata cancerStudyMetadata = getCancerStudyMetadata(dataSourceMetadata.getDownloadDirectory(), cancerStudy);
			if (cancerStudyMetadata == null) {
				continue;
			}
			Map<String,String> cancerStudyProperties = getCancerStudyProperties(dataSourceMetadata, cancerStudyMetadata);
			if (cancerStudyMetadataExists(cancerStudy)) {
				config.updateCancerStudyAttributes(cancerStudy, cancerStudyProperties);
				logMessage(LOG, "fetch(), the following study has been updated: " + cancerStudy);
			}
			else {
				config.insertCancerStudyAttributes(cancerStudyProperties);
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

	private Map<String,String> getCancerStudyProperties(DataSourcesMetadata dataSourceMetadata, CancerStudyMetadata cancerStudyMetadata)
	{
		Map<String,String> toReturn = (cancerStudyMetadataExists(cancerStudyMetadata.getStudyPath())) ?
			new HashMap<String,String>() : cancerStudyMetadata.getProperties();

	    // all cmo data needs to be vetted within the triage portal
		if (dataSourceMetadata.getDataSource().equals(DataSourcesMetadata.CMO_PIPELINE_REPOS)) {
			toReturn.put(CancerStudyMetadata.UPDATE_TRIAGE_COLUMN_KEY, "true");
			toReturn.put(CancerStudyMetadata.READY_FOR_RELEASE_COLUMN_KEY, "false");
		}
		// all other data (like DMP-IMPACT) can pass through the validation step
		else {
			toReturn.put(CancerStudyMetadata.UPDATE_TRIAGE_COLUMN_KEY, "false");
			toReturn.put(CancerStudyMetadata.READY_FOR_RELEASE_COLUMN_KEY, "true");
		}
		// this is required so that Admin.updateStudyData() will process the study
		toReturn.put(CancerStudyMetadata.REQUIRES_VALIDATION_COLUMN_KEY, "true");

		return toReturn;
	}

	private void sendNotification(List<String> cancerStudiesUpdated)
	{
		//String body = triageUpdateMessage.getText();
		//SimpleMailMessage msg = new SimpleMailMessage(triageUpdateMessage);
		for (String cancerStudy : cancerStudiesUpdated) {
			CancerStudyMetadata cancerStudyMetadata = config.getCancerStudyMetadataByName(cancerStudy);
			//body += "\n" + cancerStudyMetadata.getName();
		}
		//body += "\n";
		//msg.setText(body);
		try {
			//mailSender.send(msg);
		}
		catch (Exception e) {
			logMessage(LOG, "sendNotification(), error sending email notification:\n" + e.getMessage());
		}
	}
}

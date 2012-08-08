// package
package org.mskcc.cbio.firehose.config.internal;

// imports
import org.mskcc.cbio.firehose.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.FeedURLFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
//import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Class which implements the Config interface
 * using google docs as a backend.
 */
final class GDataImpl implements Config {

	// our logger
	private static final Log LOG = LogFactory.getLog(GDataImpl.class);

	// ref to spreadsheet client
	private SpreadsheetService spreadsheetService;

	// the following are vars set from firehose.properties 

	// google docs user
	private String gdataUser;
	@Value("${username}")
	public void setUser(final String username) { this.gdataUser = username; }

	// google docs password
	private String gdataPassword;
	@Value("${password}")
	public void setPassword(final String password) { this.gdataPassword = password; }

	// google docs spreadsheet
	private String gdataSpreadsheet;
	@Value("${spreadsheet}")
	public void setSpreadsheet(final String spreadsheet) { this.gdataSpreadsheet = spreadsheet; }

	// latest analysis run downloaded property
	private String latestAnalysisRunProperty;
	@Value("${latest_analysis_run_download}")
	public void setLatestAnalysisRunProperty(final String property) { this.latestAnalysisRunProperty = property; }

	// latest stddata run download property
	private String latestSTDDATARunProperty;
	@Value("${latest_stddata_run_download}")
	public void setLatestSTDDATARunProperty(final String property) { this.latestSTDDATARunProperty = property; }

	// analysis datatypes to download
	private String analysisDatatypesToDownload;
	@Value("${analysis_datatypes_to_download}")
	public void setAnalysisDatatypesToDownloadProperty(final String property) { this.analysisDatatypesToDownload = property; }

	// stddata datatypes to download
	private String stddataDatatypesToDownload;
	@Value("${stddata_datatypes_to_download}")
	public void setSTDDATADatatypesToDownloadProperty(final String property) { this.stddataDatatypesToDownload = property; }

	// cancer studies information
	private String cancerStudiesToDownload;
	@Value("${cancer_studies_to_download}")
	public void setCancerStudiesProperty(final String property) { this.cancerStudiesToDownload = property; }

	/**
	 * Constructor.
     *
     * Takes a Config reference.
     *
     * @param firehoseConfig Config
	 */
	public GDataImpl(final SpreadsheetService spreadsheetService) {

		// set members
		this.spreadsheetService = spreadsheetService;
	}

	/**
	 * Gets the latest analysis run.
	 *
	 * @return String
	 */
	@Override
	public String getLatestAnalysisRunDownloaded() {
		return getPropertyString(latestAnalysisRunProperty);
	}

	/**
	 * Sets the latest analysis run.
	 *
	 * @param String
	 */
	@Override
	public void setLatestAnalysisRunDownloaded(final String latestAnalysisRun) {
		setPropertyString(latestAnalysisRunProperty, latestAnalysisRun);
	}

	/**
	 * Gets the latest STDDATA run.
	 *
	 * @return String
	 */
	@Override
	public String getLatestSTDDATARunDownloaded() {
		return getPropertyString(latestSTDDATARunProperty);
	}

	/**
	 * Sets the latest STDDATA run.
	 *
	 * @param String
	 */
	@Override
	public void setLatestSTDDATARunDownloaded(final String latestSTDDATARun) {
		setPropertyString(latestSTDDATARunProperty, latestSTDDATARun);
	}

	/**
	 * Gets the analysis datatypes to download from the firehose.
	 *
	 * @return String
	 */
	@Override
	public String getAnalysisDatatypes() {

		//String datatypes = getPropertyString(analysisDatatypesToDownload);
		//return (datatypes.length() > 0) ? Arrays.asList(datatypes.split(" ")) : new ArrayList();
		return getPropertyString(analysisDatatypesToDownload);
	}

	/**
	 * Gets the stddata datatypes to download from the firehose.
	 *
	 * @return String
	 */
	@Override
	public String getSTDDATADatatypes() {

		//String datatypes = getPropertyString(stddataDatatypesToDownload);
		//return (datatypes.length() > 0) ? Arrays.asList(datatypes.split(" ")) : new ArrayList();
		return getPropertyString(stddataDatatypesToDownload);
	}

	/**
	 * Gets a list of cancer studies to download.
	 *
	 * @return String
	 */
	@Override
	public String getCancerStudiesToDownload() {

		String toReturn = "";

		if (LOG.isInfoEnabled()) {
			LOG.info("getCancerStudiesToDownload()");
		}

		// parse the property argument
		String[] properties = cancerStudiesToDownload.split(":");
		if (properties.length != 3) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getCancerStudiesToDownload: " +
						 cancerStudiesToDownload + ".  Should be worsheet:cancer_study_id:download_flag.");
			}
			return toReturn;
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(properties[0]);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				if (feed != null && feed.getEntries().size() > 0) {
					for (ListEntry entry : feed.getEntries()) {
						String downloadFlag = entry.getCustomElements().getValue(properties[2]);
						if (downloadFlag != null && downloadFlag.equals("yes")) {
							String cancerStudy = entry.getCustomElements().getValue(properties[1]);
							if (cancerStudy != null) {
								if (LOG.isInfoEnabled()) {
									LOG.info("Adding cancer study: " + cancerStudy + " to download list.");
								}
								toReturn += cancerStudy + " ";
								continue;
							}
						}
						else if (downloadFlag != null && downloadFlag.equals("no")) {
							continue;
						}
						// only get here if problem, log it
						if (LOG.isInfoEnabled()) {
							LOG.info("Cannot find cancer study and/or download property in entry list!");
						}
					}
				}
				else {
					if (LOG.isInfoEnabled()) {
						LOG.info("Worksheet contains no entries!");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// outta here
		return toReturn.trim();
	}

	/**
	 * Gets the spreadsheet.
	 *
	 * @returns SpreadsheetEntry
	 * @throws Exception
	 */
	private SpreadsheetEntry getSpreadsheet() throws Exception {

		FeedURLFactory factory = FeedURLFactory.getDefault();
		SpreadsheetFeed feed = spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry entry : feed.getEntries()) {
			if (entry.getTitle().getPlainText().equals(gdataSpreadsheet)) {
				return entry;
			}
		}
		
		// outta here
		return null;
	}

	/**
	 * Gets the worksheet feed.
	 *
	 * @returns WorksheetFeed
	 * @throws Exception
	 */
	private WorksheetEntry getWorksheet(final String gdataWorksheet) throws Exception {

		// first get the spreadsheet
		SpreadsheetEntry spreadsheet = getSpreadsheet();
		if (spreadsheet != null) {
			WorksheetFeed worksheetFeed = spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
				if (worksheet.getTitle().getPlainText().equals(gdataWorksheet)) {
					return worksheet;
				}
			}
		}

		// outta here
		return null;
	}

	/**
	 * authenticate with google spreadsheet client
	 *
	 * @throws Exception
	 */
	private void login() throws Exception {

		spreadsheetService.setUserCredentials(gdataUser, gdataPassword);
	}

	/**
	 * Gets the value of the desired property.
	 *
	 * @param propertyName String
	 * @return String
	 *
	 * Note, propertyName is worksheet:property_name pair
	 */
	private String getPropertyString(final String propertyName) {

		if (LOG.isInfoEnabled()) {
			LOG.info("getProperty(" + propertyName + ")");
		}

		// parse the property argument
		String[] properties = propertyName.split(":");
		if (properties.length != 2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getProperty: " + propertyName + ".  Should be worsheet:property_name.");
			}
			return "";
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(properties[0]);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				if (feed != null && feed.getEntries().size() > 0) {
					ListEntry entry = feed.getEntries().get(0);
					String propertyValue = entry.getCustomElements().getValue(properties[1]);
					if (propertyValue == null) {
						if (LOG.isInfoEnabled()) {
							LOG.info("Cannot find property in entry list!");
						}
						return "";
					}
					else {
						if (LOG.isInfoEnabled()) {
							LOG.info("Returning propertyValue: " + propertyValue);
						}
						return propertyValue;
					}
				}
				else {
					if (LOG.isInfoEnabled()) {
						LOG.info("Worksheet contains no entries!");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// should not get here
		return "";
	}

	/**
	 * Sets the property value.
	 *
	 * @param propertyName String
	 * @param propertyValue String
	 *
	 * Note, propertyName is worksheet:property_name pair
	 */
	private void setPropertyString(final String propertyName, final String propertyValue) {

		if (LOG.isInfoEnabled()) {
			LOG.info("setProperty(" + propertyName + " : " + propertyValue + ")");
		}

		// parse the property argument
		String[] properties = propertyName.split(":");
		if (properties.length != 2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getProperty: " + propertyName + ".  Should be worsheet:property_name.");
			}
			return;
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(properties[0]);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				ListEntry entry = feed.getEntries().get(0);
				if (entry != null) {
					entry.getCustomElements().setValueLocal(properties[1], propertyValue);
					entry.update();
					if (LOG.isInfoEnabled()) {
						LOG.info("Property has been successfully set!");
					}
				}
				else {
					if (LOG.isInfoEnabled()) {
						LOG.info("Worksheet contains no entries!");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

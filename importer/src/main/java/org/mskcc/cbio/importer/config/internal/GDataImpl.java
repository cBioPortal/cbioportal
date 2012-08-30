// package
package org.mskcc.cbio.importer.config.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

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

import java.util.ArrayList;
import java.util.Collection;
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

	// the following are vars set from importer.properties 

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

	// cancer studies metadata
	private String cancerStudiesMetadataProperty;
	@Value("${cancer_studies_metadata}")
	public void setCancerStudiesMetadataProperty(final String property) { this.cancerStudiesMetadataProperty = property; }

	// datatypes metadata
	private String datatypesMetadataProperty;
	@Value("${datatypes_metadata}")
	public void setDatatypesMetadataProperty(final String property) { this.datatypesMetadataProperty = property; }

	/**
	 * Constructor.
     *
     * Takes a ref to the gdata spreadsheet service.
     *
     * @param spreadsheetService SpreadsheetService
	 */
	public GDataImpl(final SpreadsheetService spreadsheetService) {

		// set members
		this.spreadsheetService = spreadsheetService;
	}

	/**
	 * Gets the latest analysis run.
	 *
	 * Returns the date of the latest analysis run
	 * processed by the importer as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	@Override
	public String getLatestAnalysisRunDownloaded() {
		return getPropertyString(latestAnalysisRunProperty);
	}

	/**
	 * Sets the latest analysis run processed by the importer.  Argument
	 * should be of the form "MM/dd/yyyy".
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
	 * Returns the date of the latest stddata run
	 * downloaded by the importer as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	@Override
	public String getLatestSTDDATARunDownloaded() {
		return getPropertyString(latestSTDDATARunProperty);
	}

	/**
	 * Sets the latest stddata run processed by the importer.  Argument
	 * should be of the form "MM/dd/yyyy".
	 *
	 * @param String
	 */
	@Override
	public void setLatestSTDDATARunDownloaded(final String latestSTDDATARun) {
		setPropertyString(latestSTDDATARunProperty, latestSTDDATARun);
	}

	/**
	 * Gets a collection of CancerStudyMetadata.
	 *
	 * @return Collection<CancerStudyMetadata>
	 */
	@Override
	public Collection<CancerStudyMetadata> getCancerStudyMetadata() {

		Collection<CancerStudyMetadata> toReturn = new ArrayList<CancerStudyMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getCancerStudyMetadata()");
		}

		// parse the property argument
		String[] properties = cancerStudiesMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getCancerStudyMetadata: " +
						 cancerStudiesMetadataProperty +
						 ".  Should be worsheet:cancerstudyid:cancerstudydescription:download:portals.");
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
						toReturn.add(new CancerStudyMetadata(entry.getCustomElements().getValue(properties[1]),
															 entry.getCustomElements().getValue(properties[2]),
															 new Boolean(entry.getCustomElements().getValue(properties[3])),
															 entry.getCustomElements().getValue(properties[4])));
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
		return toReturn;
	}

	/**
	 * Gets a collection of DatatypeMetadata.
	 *
	 * @return Collection<DatatypeMetadata>
	 */
	@Override
	public Collection<DatatypeMetadata> getDatatypeMetadata() {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getDatatypeMetadata()");
		}

		// parse the property argument
		String[] properties = datatypesMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getDatatypeMetadata: " +
						 datatypesMetadataProperty +
						 ".  Should be worsheet:datatype:packagefilename:datafilename:overridefilename.");
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
						DatatypeMetadata.DATATYPE datatype =
							DatatypeMetadata.DATATYPE.valueOf(entry.getCustomElements().getValue(properties[1]));
						toReturn.add(new DatatypeMetadata(datatype,
														  entry.getCustomElements().getValue(properties[2]),
														  entry.getCustomElements().getValue(properties[3]),
														  entry.getCustomElements().getValue(properties[4])));
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
		return toReturn;

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

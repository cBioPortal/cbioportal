// package
package org.mskcc.cbio.importer.config.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.FirehoseDatatypeMetadata;
import org.mskcc.cbio.importer.model.FirehoseDownloadMetadata;

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

	// tumor types metadata
	private String tumorTypesMetadataProperty;
	@Value("${tumor_types_metadata}")
	public void setTumorTypesMetadataProperty(final String property) { this.tumorTypesMetadataProperty = property; }

	// datatype metadata
	private String datatypesMetadataProperty;
	@Value("${datatypes_metadata}")
	public void setDatatypesMetadataProperty(final String property) { this.datatypesMetadataProperty = property; }

	// portal metadata
	private String portalsMetadataProperty;
	@Value("${portals_metadata}")
	public void setPortalsMetadataProperty(final String property) { this.portalsMetadataProperty = property; }

	// firehose datatypes metadata
	private String firehoseDatatypesMetadataProperty;
	@Value("${firehose_datatypes_metadata}")
	public void setFirehoseDatatypesMetadataProperty(final String property) { this.firehoseDatatypesMetadataProperty = property; }

	// firehose download metadata
	private String firehoseDownloadMetadataProperty;
	@Value("${firehose_download_metadata}")
	public void setFirehoseDownloadMetadataProperty(final String property) { this.firehoseDownloadMetadataProperty = property; }

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
	 * Gets a collection of TumorTypeMetadata.
	 *
	 * @return Collection<TumorTypeMetadata>
	 */
	@Override
	public Collection<TumorTypeMetadata> getTumorTypeMetadata() {

		Collection<TumorTypeMetadata> toReturn = new ArrayList<TumorTypeMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getTumorTypeMetadata()");
		}

		// parse the property argument
		String[] properties = tumorTypesMetadataProperty.split(":");
		if (properties.length != 4) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getTumorTypeMetadata: " + tumorTypesMetadataProperty);
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
						toReturn.add(new TumorTypeMetadata(entry.getCustomElements().getValue(properties[2]),
															 entry.getCustomElements().getValue(properties[3]),
															 new Boolean(entry.getCustomElements().getValue(properties[1]))));
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
		if (properties.length != 11) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getDatatypeMetadata: " + datatypesMetadataProperty);
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
						toReturn.add(new DatatypeMetadata(entry.getCustomElements().getValue(properties[1]),
                                                          entry.getCustomElements().getValue(properties[2]),
                                                          entry.getCustomElements().getValue(properties[3]),
                                                          entry.getCustomElements().getValue(properties[4]),
                                                          entry.getCustomElements().getValue(properties[5]),
                                                          entry.getCustomElements().getValue(properties[6]),
                                                          entry.getCustomElements().getValue(properties[7]),
                                                          entry.getCustomElements().getValue(properties[8]),
                                                          new Boolean(entry.getCustomElements().getValue(properties[9])),
                                                          entry.getCustomElements().getValue(properties[10])));
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
	 * Gets a PortalMetadata object given a portal name.
	 *
     * @param portal String
	 * @return PortalMetadata
	 */
    @Override
	public PortalMetadata getPortalMetadata(String portal) {

        PortalMetadata toReturn = null;

		if (LOG.isInfoEnabled()) {
			LOG.info("getPortalMetadata(), portal: " + portal);
		}

		// parse the property argument
		String[] properties = portalsMetadataProperty.split(":");
		if (properties.length != 7) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getPortalMetadata: " + portalsMetadataProperty);
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
                        if (entry.getCustomElements().getValue(properties[1]).equals(portal)) {
                                toReturn = new PortalMetadata(entry.getCustomElements().getValue(properties[1]),
                                                              entry.getCustomElements().getValue(properties[2]),
                                                              entry.getCustomElements().getValue(properties[3]),
                                                              entry.getCustomElements().getValue(properties[4]),
                                                              entry.getCustomElements().getValue(properties[5]),
                                                              entry.getCustomElements().getValue(properties[6]));
                                break;
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
        return toReturn;
    }

	/**
	 * Gets a collection of FirehoseDatatypeMetadata.
	 *
	 * @return Collection<FirehoseDatatypeMetadata>
	 */
    @Override
	public Collection<FirehoseDatatypeMetadata> getFirehoseDatatypeMetadata() {

		Collection<FirehoseDatatypeMetadata> toReturn = new ArrayList<FirehoseDatatypeMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getFirehoseDatatypeMetadata()");
		}

		// parse the property argument
		String[] properties = firehoseDatatypesMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getFirehoseDatatypeMetadata: " + firehoseDatatypesMetadataProperty);
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
						//DatatypeMetadata.DATATYPE datatype =
						//	DatatypeMetadata.DATATYPE.valueOf(entry.getCustomElements().getValue(properties[2]));
						toReturn.add(new FirehoseDatatypeMetadata(entry.getCustomElements().getValue(properties[2]),
                                                                  new Boolean(entry.getCustomElements().getValue(properties[1])),
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
	 * Gets FirehoseDownloadMetadata.
	 *
	 * @return FirehoseDownloadMetadata
	 */
    @Override
	public FirehoseDownloadMetadata getFirehoseDownloadMetadata() {

		FirehoseDownloadMetadata toReturn = null;

		if (LOG.isInfoEnabled()) {
			LOG.info("getFirehoseDownloadMetadata()");
		}

		// parse the property argument
		String[] properties = firehoseDownloadMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getFirehoseDownloadMetadata: " + firehoseDownloadMetadataProperty);
			}
			return toReturn;
		}

        // outta here
        return new FirehoseDownloadMetadata(getPropertyString(properties[0] + ":" + properties[1]),
                                            getPropertyString(properties[0] + ":" + properties[2]),
                                            getPropertyString(properties[0] + ":" + properties[3]),
                                            getPropertyString(properties[0] + ":" + properties[4]));
	}

	/**
	 * Sets FirehoseDownloadMetadata.  Really only used to store
     * stddata/analysis run dates.
	 *
     * @param firehoseDownloadMetadata FirehoseDownloadMetadata
	 * @return FirehoseDownloadMetadata
	 */
    @Override
	public void setFirehoseDownloadMetadata(final FirehoseDownloadMetadata firehoseDownloadMetadata) {

		if (LOG.isInfoEnabled()) {
			LOG.info("setFirehoseDownloadMetadata()");
		}

		// parse the property argument
		String[] properties = firehoseDownloadMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to setFirehoseDownloadMetadata: " + firehoseDownloadMetadataProperty);
			}
			return;
		}

        setPropertyString(properties[0] + ":" + properties[2], firehoseDownloadMetadata.getLatestAnalysisRunDownloaded());
        setPropertyString(properties[0] + ":" + properties[4], firehoseDownloadMetadata.getLatestSTDDATARunDownloaded());
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

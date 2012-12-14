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
package org.mskcc.cbio.importer.config.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CaseIDFilterMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;

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

	// case id filters metadata
	private String caseIDFiltersMetadataProperty;
	@Value("${case_id_filters_metadata}")
	public void setCaseIDFiltersMetadataProperty(final String property) { this.caseIDFiltersMetadataProperty = property; }

	// case list metadata
	private String caseListMetadataProperty;
	@Value("${case_lists_metadata}")
	public void setCaseListMetadataProperty(final String property) { this.caseListMetadataProperty = property; }

	// portal metadata
	private String portalsMetadataProperty;
	@Value("${portals_metadata}")
	public void setPortalsMetadataProperty(final String property) { this.portalsMetadataProperty = property; }

	// reference metadata
	private String referenceMetadataProperty;
	@Value("${reference_metadata}")
	public void setReferenceMetadataProperty(final String property) { this.referenceMetadataProperty = property; }

	// data source metadata
	private String dataSourceMetadataProperty;
	@Value("${data_sources_metadata}")
	public void setDataSourceMetadataProperty(final String property) { this.dataSourceMetadataProperty = property; }

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
		if (properties.length != 16) {
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
														  new Boolean(entry.getCustomElements().getValue(properties[2])),
                                                          entry.getCustomElements().getValue(properties[3]),
                                                          entry.getCustomElements().getValue(properties[4]),
                                                          entry.getCustomElements().getValue(properties[5]),
                                                          entry.getCustomElements().getValue(properties[6]),
                                                          entry.getCustomElements().getValue(properties[7]),
                                                          entry.getCustomElements().getValue(properties[8]),
														  new Boolean(entry.getCustomElements().getValue(properties[9])),
                                                          entry.getCustomElements().getValue(properties[10]),
                                                          entry.getCustomElements().getValue(properties[11]),
														  entry.getCustomElements().getValue(properties[12]),
														  new Boolean(entry.getCustomElements().getValue(properties[13])),
														  entry.getCustomElements().getValue(properties[14]),
                                                          entry.getCustomElements().getValue(properties[15])));
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
	 * Gets a collection of CaseIDFilterMetadata.
	 *
	 * @return Collection<CaseIDFilterMetadata>
	 */
	@Override
	public Collection<CaseIDFilterMetadata> getCaseIDFilterMetadata() {

		Collection<CaseIDFilterMetadata> toReturn = new ArrayList<CaseIDFilterMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getCaseIDFilterMetadata()");
		}

		// parse the property argument
		String[] properties = caseIDFiltersMetadataProperty.split(":");
		if (properties.length != 4) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getCaseIDFilterMetadata: " + caseIDFiltersMetadataProperty);
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
						toReturn.add(new CaseIDFilterMetadata(entry.getCustomElements().getValue(properties[1]),
															  entry.getCustomElements().getValue(properties[2])));
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
	 * Gets a collection of CaseListMetadata.
	 *
	 * @return Collection<CaseListMetadata>
	 */
	@Override
	public Collection<CaseListMetadata> getCaseListMetadata() {

		Collection<CaseListMetadata> toReturn = new ArrayList<CaseListMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getCaseListMetadata()");
		}

		// parse the property argument
		String[] properties = caseListMetadataProperty.split(":");
		if (properties.length != 8) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getCaseListMetadata: " + caseListMetadataProperty);
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
						toReturn.add(new CaseListMetadata(entry.getCustomElements().getValue(properties[1]),
														  entry.getCustomElements().getValue(properties[2]),
														  entry.getCustomElements().getValue(properties[3]),
														  entry.getCustomElements().getValue(properties[4]),
														  entry.getCustomElements().getValue(properties[5]),
														  entry.getCustomElements().getValue(properties[6]),
														  entry.getCustomElements().getValue(properties[7])));
															
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
		if (properties.length != 8) {
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
                                                              entry.getCustomElements().getValue(properties[6]),
                                                              entry.getCustomElements().getValue(properties[7]));
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
	 * Gets ReferenceMetadata for the given referenceType.
	 *
	 * @param referenceType String
	 * @return Collection<ReferenceMetadata>
	 */
    @Override
	public Collection<ReferenceMetadata> getReferenceMetadata(String referenceType) {

		Collection<ReferenceMetadata> toReturn = new ArrayList<ReferenceMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getReferenceMetadata()");
		}

		// parse the property argument
		String[] properties = referenceMetadataProperty.split(":");
		if (properties.length != 6) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getReferenceMetadata: " + referenceMetadataProperty);
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
                        if (referenceType.equals(Config.ALL_METADATA) || entry.getCustomElements().getValue(properties[1]).equals(referenceType)) {
							toReturn.add(new ReferenceMetadata(entry.getCustomElements().getValue(properties[1]),
															   new Boolean(entry.getCustomElements().getValue(properties[2])),
															   entry.getCustomElements().getValue(properties[3]),
															   entry.getCustomElements().getValue(properties[4]),
															   entry.getCustomElements().getValue(properties[5])));
							if (!referenceType.equals(Config.ALL_METADATA)) break;
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

		if (toReturn.isEmpty() && LOG.isInfoEnabled()) {
			LOG.info("getReferenceMetadata(), toReturn size is 0.");
		}

        // outta here
        return toReturn;
	}

	/**
	 * Gets DataSourceMetadata for the given datasource.
	 *
	 * @param dataSource String
	 * @return Collection<DataSourceMetadata>
	 */
    @Override
	public Collection<DataSourceMetadata> getDataSourceMetadata(String dataSource) {

		Collection<DataSourceMetadata> toReturn = new ArrayList<DataSourceMetadata>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getDataSourceMetadata(): " + dataSource);
		}

		// parse the property argument
		String[] properties = dataSourceMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to getDataSourceMetadata: " + dataSourceMetadataProperty);
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
                        if (dataSource.equals(Config.ALL_METADATA) || entry.getCustomElements().getValue(properties[1]).equals(dataSource)) {
							toReturn.add(new DataSourceMetadata(entry.getCustomElements().getValue(properties[1]),
																entry.getCustomElements().getValue(properties[2]),
																entry.getCustomElements().getValue(properties[3]),
																entry.getCustomElements().getValue(properties[4])));
							if (!dataSource.equals(Config.ALL_METADATA)) break;
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

		if (toReturn.isEmpty() && LOG.isInfoEnabled()) {
			LOG.info("getDataSourceMetadata(), toReturn size is 0.");
		}

        // outta here
        return toReturn;
	}

	/**
	 * Sets DataSourceMetadata (currently only stores latest run downloaded).
	 *
     * @param dataSourceMetadata DataSourceMetadata
	 */
    @Override
	public 	void setDataSourceMetadata(final DataSourceMetadata dataSourceMetadata) {

		if (LOG.isInfoEnabled()) {
			LOG.info("setFirehoseDownloadMetadata()");
		}

		// parse the property argument
		String[] properties = dataSourceMetadataProperty.split(":");
		if (properties.length != 5) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Invalid property passed to setDataSourceMetadata: " + dataSourceMetadataProperty);
			}
			return;
		}

        setPropertyString(properties[0], properties[1], dataSourceMetadata.getDataSource(),
						  properties[3], dataSourceMetadata.getLatestRunDownload());
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
	 * @param worksheetName String
	 * @param key String
	 * @param propertyName String
	 * @param propertyValue String
	 *
	 * Note, propertyName is worksheet:property_name pair
	 */
	private void setPropertyString(final String worksheetName, final String keyColumn, final String key, final String propertyName, final String propertyValue) {

		if (LOG.isInfoEnabled()) {
			LOG.info("setProperty(" + worksheetName + " : " + key + " : " + propertyName + " : " + propertyValue + ")");
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(worksheetName);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				for (ListEntry entry : feed.getEntries()) {
					if (entry.getCustomElements().getValue(keyColumn).equals(key)) {
						entry.getCustomElements().setValueLocal(propertyName, propertyValue);
						entry.update();
						if (LOG.isInfoEnabled()) {
							LOG.info("Property has been successfully set!");
						}
					}
				}
			}
			else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Worksheet contains no entries!");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

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
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.CaseIDFilterMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.util.ClassLoader;

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

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Class which implements the Config interface
 * using google docs as a backend.
 */
final class GDataImpl implements Config {

	// our logger
	private static final Log LOG = LogFactory.getLog(GDataImpl.class);

	// ref to spreadsheet client
	private SpreadsheetService spreadsheetService;

	// for performance optimization - we only retreive worksheet data once
	ArrayList<ArrayList<String>> cancerStudiesMetadata;
	ArrayList<ArrayList<String>> caseIDFiltersMetadata;
	ArrayList<ArrayList<String>> caseListMetadata;
	ArrayList<ArrayList<String>> dataTypesMetadata;
	ArrayList<ArrayList<String>> dataSourcesMetadata;
	ArrayList<ArrayList<String>> portalsMetadata;
	ArrayList<ArrayList<String>> referenceMetadata;
	ArrayList<ArrayList<String>> tumorTypesMetadata;

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

	// tumor types worksheet
	private String tumorTypesWorksheet;
	@Value("${tumor_types_worksheet}")
	public void setTumorTypesMetadataProperty(final String property) { this.tumorTypesWorksheet = property; }

	// datatype worksheet
	private String datatypesWorksheet;
	@Value("${datatypes_worksheet}")
	public void setDatatypesMetadataProperty(final String property) { this.datatypesWorksheet = property; }

	// case id filters worksheet
	private String caseIDFiltersWorksheet;
	@Value("${case_id_filters_worksheet}")
	public void setCaseIDFiltersMetadataProperty(final String property) { this.caseIDFiltersWorksheet = property; }

	// case list worksheet
	private String caseListWorksheet;
	@Value("${case_lists_worksheet}")
	public void setCaseListMetadataProperty(final String property) { this.caseListWorksheet = property; }

	// portals worksheet
	private String portalsWorksheet;
	@Value("${portals_worksheet}")
	public void setPortalsMetadataProperty(final String property) { this.portalsWorksheet = property; }

	// reference data worksheet
	private String referenceDataWorksheet;
	@Value("${reference_data_worksheet}")
	public void setReferenceMetadataProperty(final String property) { this.referenceDataWorksheet = property; }

	// data sources worksheet
	private String dataSourcesWorksheet;
	@Value("${data_sources_worksheet}")
	public void setDataSourcesMetadataProperty(final String property) { this.dataSourcesWorksheet = property; }

	// cancer studies metadata
	private String cancerStudiesWorksheet;
	@Value("${cancer_studies_worksheet}")
	public void setCancerStudiesProperty(final String property) { this.cancerStudiesWorksheet = property; }

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
	 * Gets a TumorTypeMetadata object via tumorType.
	 * If tumorType == Config.ALL, all are returned.
	 *
	 * @param tumortype String
	 * @return TumorTypeMetadata
	 */
	@Override
	public Collection<TumorTypeMetadata> getTumorTypeMetadata(String tumorType) {

		Collection<TumorTypeMetadata> toReturn = new ArrayList<TumorTypeMetadata>();

		Collection<TumorTypeMetadata> tumorTypeMetadatas = 
			(Collection<TumorTypeMetadata>)getMetadataCollection(tumorTypesMetadata,
																 tumorTypesWorksheet,
																 "org.mskcc.cbio.importer.model.TumorTypeMetadata");
		// if user wants all, we're done
		if (tumorType.equals(Config.ALL)) {
			return tumorTypeMetadatas;
		}

		// iterate over all TumorTypeMetadata looking for match
		for (TumorTypeMetadata tumorTypeMetadata : tumorTypeMetadatas) {
            if (tumorTypeMetadata.getType().equals(tumorType)) {
				toReturn.add(tumorTypeMetadata);
            }
		}

		// outta here
		return toReturn;
	}

	/**
	 * Function to get tumor types to download as String[]
	 *
	 * @return String[]
	 */
	@Override
	public String[] getTumorTypesToDownload() {

		String toReturn = "";
		for (TumorTypeMetadata tumorTypeMetadata : getTumorTypeMetadata(Config.ALL)) {
			if (tumorTypeMetadata.getDownload()) {
				toReturn += tumorTypeMetadata.getType() + ":";
			}
		}

		// outta here
		return toReturn.split(":");
	}

	/**
	 * Gets a DatatypeMetadata object for the given datatype name.
	 * If datatype == Config.ALL, all are returned.
	 *
	 * @param datatype String
	 * @return Collection<DatatypeMetadata>
	 */
	@Override
	public Collection<DatatypeMetadata> getDatatypeMetadata(String datatype) {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();

		Collection<DatatypeMetadata> datatypeMetadatas = 
			(Collection<DatatypeMetadata>)getMetadataCollection(dataTypesMetadata,
																datatypesWorksheet,
																"org.mskcc.cbio.importer.model.DatatypeMetadata");
		// if user wants all, we're done
		if (datatype.equals(Config.ALL)) {
			return datatypeMetadatas;
		}

		for (DatatypeMetadata datatypeMetadata : datatypeMetadatas) {
            if (datatypeMetadata.getDatatype().equals(datatype)) {
				toReturn.add(datatypeMetadata);
            }
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets a collection of Datatype names for the given portal/cancer study.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @return Collection<String>
	 */
	@Override
	public Collection<DatatypeMetadata> getDatatypeMetadata(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata) {

		Collection<DatatypeMetadata> toReturn = null;

		Collection<CancerStudyMetadata> cancerStudyMetadatas =
			(Collection<CancerStudyMetadata>)getMetadataCollection(cancerStudiesMetadata,
																   cancerStudiesWorksheet,
																   "org.mskcc.cbio.importer.model.CancerStudyMetadata");

		// get portal name which is column header in cancer studies matrix.  note: google strips out "-" from column headers
		String portalName = (portalMetadata.getName().contains("-")) ?
			portalMetadata.getName().replaceAll("-", "") : portalMetadata.getName();

		// get portal-column index in the cancer studies worksheet
		int portalColumnIndex = cancerStudiesMetadata.get(0).indexOf(portalName);
		if (portalColumnIndex == -1) return toReturn;

		// iterate over all studies in worksheet and find row whose first element is cancer study (path)
		for (ArrayList<String> matrixRow : cancerStudiesMetadata) {
			if (matrixRow.get(0).equals(cancerStudyMetadata.getStudyPath())) {
				// the datatypes for the portal/cancer_study is the value of the cell
				String datatypesIndicator = matrixRow.get(portalColumnIndex);
				if (datatypesIndicator.equalsIgnoreCase(CancerStudyMetadata.CANCER_STUDY_IN_PORTAL_INDICATOR)) {
					// all datatypes are desired
					toReturn = getDatatypeMetadata(Config.ALL);
				}
				else {
					// a delimited list of datatypes have been requested
					toReturn = new ArrayList<DatatypeMetadata>();
					for (String datatype : datatypesIndicator.split(DatatypeMetadata.DATATYPES_DELIMITER)) {
						toReturn.add(getDatatypeMetadata(datatype).iterator().next());
					}
				}
				break;
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Function to get datatypes to download as String[]
	 *
	 * @param dataSourcesMetadata DataSourcesMetadata
	 * @return String[]
	 * @throws Exception
	 */
	@Override
	public String[] getDatatypesToDownload(DataSourcesMetadata dataSourcesMetadata) throws Exception {

		HashSet<String> toReturn = new HashSet<String>();
		for (DatatypeMetadata datatypeMetadata : getDatatypeMetadata(Config.ALL)) {
			if (datatypeMetadata.isDownloaded()) {
				Method downloadArchivesMethod = datatypeMetadata.getDownloadArchivesMethod(dataSourcesMetadata.getDataSource());
				toReturn.addAll((Set<String>)downloadArchivesMethod.invoke(datatypeMetadata, null));
			}
		}

		// outta here
		return toReturn.toArray(new String[0]);
	}

	/**
	 * Function to determine the datatype(s)
	 * of the datasource file (the file that was fetched from a datasource).
	 *
	 * @param dataSourcesMetadata DataSourcesMetadata
	 * @param filename String
	 * @return Collection<DatatypeMetadata>
	 * @throws Exception
	 */
	@Override
	public Collection<DatatypeMetadata> getFileDatatype(DataSourcesMetadata dataSourcesMetadata, final String filename)  throws Exception {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();
		for (DatatypeMetadata datatypeMetadata : getDatatypeMetadata(Config.ALL)) {
			Method downloadArchivesMethod = datatypeMetadata.getDownloadArchivesMethod(dataSourcesMetadata.getDataSource());
			for (String archive : (Set<String>)downloadArchivesMethod.invoke(datatypeMetadata, null)) {
				if (filename.contains(archive)) {
					toReturn.add(datatypeMetadata);
				}
			}
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

		return (Collection<CaseIDFilterMetadata>)getMetadataCollection(caseIDFiltersMetadata,
																	   caseIDFiltersWorksheet,
																	   "org.mskcc.cbio.importer.model.CaseIDFilterMetadata");
	}

	/**
	 * Gets a collection of CaseListMetadata.
	 *
	 * @return Collection<CaseListMetadata>
	 */
	@Override
	public Collection<CaseListMetadata> getCaseListMetadata() {

		return (Collection<CaseListMetadata>)getMetadataCollection(caseListMetadata,
																   caseListWorksheet,
																   "org.mskcc.cbio.importer.model.CaseListMetadata");
	}

	/**
	 * Gets a PortalMetadata object given a portal name.
	 *
     * @param portal String
	 * @return PortalMetadata
	 */
    @Override
	public PortalMetadata getPortalMetadata(String portal) {

		Collection<PortalMetadata> portalMetadatas =
			(Collection<PortalMetadata>)getMetadataCollection(portalsMetadata,
															  portalsWorksheet,
															  "org.mskcc.cbio.importer.model.PortalMetadata");

		for (PortalMetadata portalMetadata : portalMetadatas) {
			if (portalMetadata.getName().equals(portal)) {
				return portalMetadata;
			}
		}

		// outta here
		return null;
    }

	/**
	 * Gets ReferenceMetadata for the given referenceType.
	 * If referenceType == Config.ALL, all are returned.
	 *
	 * @param referenceType String
	 * @return Collection<ReferenceMetadata>
	 */
    @Override
	public Collection<ReferenceMetadata> getReferenceMetadata(String referenceType) {

		Collection<ReferenceMetadata> toReturn = new ArrayList<ReferenceMetadata>();

		Collection<ReferenceMetadata> referenceMetadatas =
			(Collection<ReferenceMetadata>)getMetadataCollection(referenceMetadata,
																 referenceDataWorksheet,
																 "org.mskcc.cbio.importer.model.ReferenceMetadata");
		// if user wants all, we're done
		if (referenceType.equals(Config.ALL)) {
			return referenceMetadatas;
		}

		// iterate over all ReferenceMetadata looking for match
		for (ReferenceMetadata referenceMetadata : referenceMetadatas) {
			if (referenceMetadata.getReferenceType().equals(referenceType)) {
				toReturn.add(referenceMetadata);
				break;
			}
		}

        // outta here
        return toReturn;
	}

	/**
	 * Gets DataSourcesMetadata for the given datasource.  If dataSource == Config.ALL,
	 * all are returned.
	 *
	 * @param dataSource String
	 * @return Collection<DataSourcesMetadata>
	 */
    @Override
	public Collection<DataSourcesMetadata> getDataSourcesMetadata(String dataSource) {

		Collection<DataSourcesMetadata> toReturn = new ArrayList<DataSourcesMetadata>();

		Collection<DataSourcesMetadata> dataSourceMetadatas =
			(Collection<DataSourcesMetadata>)getMetadataCollection(dataSourcesMetadata,
																   dataSourcesWorksheet,
																   "org.mskcc.cbio.importer.model.DataSourcesMetadata");
		// if user wants all, we're done
		if (dataSource.equals(Config.ALL)) {
			return dataSourceMetadatas;
		}

		// iterate over all DataSourcesMetadata looking for match
		for (DataSourcesMetadata dataSourceMetadata : dataSourceMetadatas) {
			if (dataSourceMetadata.getDataSource().equals(dataSource)) {
				toReturn.add(dataSourceMetadata);
				break;
			}
		}

        // outta here
        return toReturn;
	}

	/**
	 * Gets all the cancer studies for a given portal.
	 *
     * @param portal String
	 * @return Collection<CancerStudyMetadata>
	 */
	@Override
	public Collection<CancerStudyMetadata> getCancerStudyMetadata(String portalName) {

		Collection<CancerStudyMetadata> toReturn = new ArrayList<CancerStudyMetadata>();

		if (cancerStudiesMetadata == null) {
			cancerStudiesMetadata = getWorksheetData(cancerStudiesWorksheet);
		}

		// get portal-column index in the cancer studies worksheet
		int portalColumnIndex = cancerStudiesMetadata.get(0).indexOf(portalName);
		if (portalColumnIndex == -1) return toReturn;

		// iterate over all studies in worksheet and determine if 
		// the value at the row and portal/column intersection is not empty
		// (we start at one, because row 0 is the column headers)
		for (int lc = 1; lc < cancerStudiesMetadata.size(); lc++) {
			ArrayList<String> matrixRow = cancerStudiesMetadata.get(lc);
			String datatypesIndicator = matrixRow.get(portalColumnIndex);
			if (datatypesIndicator != null && datatypesIndicator.length() > 0) {
				CancerStudyMetadata cancerStudyMetadata = 
					new CancerStudyMetadata(matrixRow.toArray(new String[0]));
				// get tumor type metadata
				Collection<TumorTypeMetadata> tumorType = getTumorTypeMetadata(cancerStudyMetadata.getTumorType());
				cancerStudyMetadata.setTumorTypeMetadata(tumorType.iterator().next());
				// add to return set
				toReturn.add(cancerStudyMetadata);
			}
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
	 * Constructs a collection of objects of the given classname from the given matrix.
	 *
	 * @param metadataMatrix ArrayList<ArrayList<String>>
	 * @parma worksheet String
	 * @param className String
	 * @return Collection<?>
	 */
	private Collection<?> getMetadataCollection(ArrayList<ArrayList<String>> metadataMatrix,
												String worksheet, String className) {

		Collection<Object> toReturn = new ArrayList<Object>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getMetadataCollection(): " + worksheet + " : " + className);
		}

		// lazy loading  - if the matrix is null, load now
		if (metadataMatrix == null) {
			metadataMatrix = getWorksheetData(worksheet);
		}

		// we start at one, because row 0 is the column headers
		for (int lc = 1; lc < metadataMatrix.size(); lc++) {
			Object[] args = { metadataMatrix.get(lc).toArray(new String[0]) };
			try {
				toReturn.add(ClassLoader.getInstance(className, args));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Helper function to retrieve the given google worksheet data matrix.
	 * as a list of string lists.
	 *
	 * @param worksheet String
	 * @return ArrayList<ArrayList<String>>
	 */
	private ArrayList<ArrayList<String>> getWorksheetData(String worksheetName) {

		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getWorksheetData(): " + worksheetName);
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(worksheetName);
			if (worksheet != null) {
				ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				if (feed != null && feed.getEntries().size() > 0) {
					boolean needHeaders = true;
					for (ListEntry entry : feed.getEntries()) {
						if (needHeaders) {
							ArrayList<String> headers = new ArrayList<String>(entry.getCustomElements().getTags());
							toReturn.add(headers);
							needHeaders = false;
						}
						ArrayList<String> customElements = new ArrayList<String>();
						for (String tag : toReturn.get(0)) {
							String value = entry.getCustomElements().getValue(tag);
							if (value == null) value = "";
							customElements.add(value);
						}
						toReturn.add(customElements);
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
}

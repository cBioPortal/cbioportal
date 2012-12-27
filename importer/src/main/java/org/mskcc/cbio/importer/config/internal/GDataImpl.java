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
class GDataImpl implements Config {

	// our logger
	private static Log LOG = LogFactory.getLog(GDataImpl.class);

	// google docs user
	private String gdataUser;
	// google docs password
	private String gdataPassword;
	// ref to spreadsheet client
	private SpreadsheetService spreadsheetService;

	// for performance optimization - we only retreive worksheet data once
	ArrayList<ArrayList<String>> cancerStudiesMatrix;
	ArrayList<ArrayList<String>> caseIDFiltersMatrix;
	ArrayList<ArrayList<String>> caseListMatrix;
	ArrayList<ArrayList<String>> datatypesMatrix;
	ArrayList<ArrayList<String>> dataSourcesMatrix;
	ArrayList<ArrayList<String>> portalsMatrix;
	ArrayList<ArrayList<String>> referenceMatrix;
	ArrayList<ArrayList<String>> tumorTypesMatrix;

	/**
	 * Constructor.
     *
     * Constructor args are passed viaw applicationContext.  We do this so that all our
	 *  metadata objects can be retrieved during construction of this class.  Which will
	 * prevent us from having to access google more than once.  Of course any changes to
	 * the google docs will not be reflected in this class until its next instantiation.
     *
	 * @param gdataUser String
	 * @param gdataPassword String
     * @param spreadsheetService SpreadsheetService
	 * @param gdataSpreadsheet String
	 * @param tumorTypesWorksheet String
	 * @param datatypesWorksheet String
	 * @param caseIDFiltersWorksheet String
	 * @param caseListWorksheet String
	 * @param portalsWorksheet String
	 * @param referenceDataWorksheet String
	 * @param dataSourceseWorksheet String
	 * @param cancerStudiesWorksheet String
	 */
	public GDataImpl(String gdataUser, String gdataPassword, SpreadsheetService spreadsheetService,
					 String gdataSpreadsheet, String tumorTypesWorksheet, String datatypesWorksheet,
					 String caseIDFiltersWorksheet, String caseListWorksheet, String portalsWorksheet,
					 String referenceDataWorksheet, String dataSourcesWorksheet, String cancerStudiesWorksheet) {

		// set members
		this.gdataUser = gdataUser;
		this.gdataPassword = gdataPassword;
		this.spreadsheetService = spreadsheetService;

		tumorTypesMatrix = getWorksheetData(gdataSpreadsheet, tumorTypesWorksheet);
		datatypesMatrix = getWorksheetData(gdataSpreadsheet, datatypesWorksheet);
		caseIDFiltersMatrix = getWorksheetData(gdataSpreadsheet, caseIDFiltersWorksheet);
		caseListMatrix = getWorksheetData(gdataSpreadsheet, caseListWorksheet);
		portalsMatrix = getWorksheetData(gdataSpreadsheet, portalsWorksheet);
		referenceMatrix = getWorksheetData(gdataSpreadsheet, referenceDataWorksheet);
		dataSourcesMatrix = getWorksheetData(gdataSpreadsheet, dataSourcesWorksheet);
		cancerStudiesMatrix = getWorksheetData(gdataSpreadsheet, cancerStudiesWorksheet);
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
			(Collection<TumorTypeMetadata>)getMetadataCollection(tumorTypesMatrix,
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
	public Collection<DatatypeMetadata> getFileDatatype(DataSourcesMetadata dataSourcesMetadata, String filename)  throws Exception {

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
			(Collection<DatatypeMetadata>)getMetadataCollection(datatypesMatrix,
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

		// get portal name which is column header in cancer studies matrix.  note: google strips out "-" from column headers
		String portalName = (portalMetadata.getName().contains("-")) ?
			portalMetadata.getName().replaceAll("-", "") : portalMetadata.getName();

		// get portal-column index in the cancer studies worksheet
		int portalColumnIndex = cancerStudiesMatrix.get(0).indexOf(portalName);
		if (portalColumnIndex == -1) return toReturn;

		// iterate over all studies in worksheet and find row whose first element is cancer study (path)
		for (ArrayList<String> matrixRow : cancerStudiesMatrix) {
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
	 * Gets a collection of CaseIDFilterMetadata.
	 *
	 * @param filterName String
	 * @return Collection<CaseIDFilterMetadata>
	 */
	@Override
	public Collection<CaseIDFilterMetadata> getCaseIDFilterMetadata(String filterName) {

		Collection<CaseIDFilterMetadata> toReturn = new ArrayList<CaseIDFilterMetadata>();

		Collection<CaseIDFilterMetadata> caseIDFilterMetadatas = 
			(Collection<CaseIDFilterMetadata>)getMetadataCollection(caseIDFiltersMatrix,
																	"org.mskcc.cbio.importer.model.CaseIDFilterMetadata");

		// if user wants all, we're done
		if (filterName.equals(Config.ALL)) {
			return caseIDFilterMetadatas;
		}

		for (CaseIDFilterMetadata caseIDFilterMetadata : caseIDFilterMetadatas) {
			if (caseIDFilterMetadata.getFilterName().equals(filterName)) {
				toReturn.add(caseIDFilterMetadata);
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets a collection of CaseListMetadata.
	 * If caseListFilename == Config.ALL, all are returned.
	 *
	 * @param caseListFilename String
	 * @return Collection<CaseListMetadata>
	 */
	@Override
	public Collection<CaseListMetadata> getCaseListMetadata(String caseListFilename) {

		Collection<CaseListMetadata> toReturn = new ArrayList<CaseListMetadata>();

		Collection<CaseListMetadata> caseListMetadatas = 
			(Collection<CaseListMetadata>)getMetadataCollection(caseListMatrix,
																"org.mskcc.cbio.importer.model.CaseListMetadata");

		// if user wants all, we're done
		if (caseListFilename.equals(Config.ALL)) {
			return caseListMetadatas;
		}

		for (CaseListMetadata caseListMetadata : caseListMetadatas) {
			if (caseListMetadata.getCaseListFilename().equals(caseListFilename)) {
				toReturn.add(caseListMetadata);
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets a PortalMetadata object given a portal name.
	 *
     * @param portal String
	 * @return Collection<PortalMetadata>
	 */
    @Override
	public Collection<PortalMetadata> getPortalMetadata(String portalName) {

		Collection<PortalMetadata> toReturn = new ArrayList<PortalMetadata>();

		Collection<PortalMetadata> portalMetadatas =
			(Collection<PortalMetadata>)getMetadataCollection(portalsMatrix,
															  "org.mskcc.cbio.importer.model.PortalMetadata");

		// if user wants all, we're done
		if (portalName.equals(Config.ALL)) {
			return portalMetadatas;
		}

		for (PortalMetadata portalMetadata : portalMetadatas) {
			if (portalMetadata.getName().equals(portalName)) {
				toReturn.add(portalMetadata);
			}
		}

		// outta here
		return toReturn;
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
			(Collection<ReferenceMetadata>)getMetadataCollection(referenceMatrix,
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
			(Collection<DataSourcesMetadata>)getMetadataCollection(dataSourcesMatrix,
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

		// get portal-column index in the cancer studies worksheet
		int portalColumnIndex = cancerStudiesMatrix.get(0).indexOf(portalName);
		if (portalColumnIndex == -1) return toReturn;

		// iterate over all studies in worksheet and determine if 
		// the value at the row and portal/column intersection is not empty
		// (we start at one, because row 0 is the column headers)
		for (int lc = 1; lc < cancerStudiesMatrix.size(); lc++) {
			ArrayList<String> matrixRow = cancerStudiesMatrix.get(lc);
			String datatypesIndicator = matrixRow.get(portalColumnIndex);
			if (datatypesIndicator != null && datatypesIndicator.length() > 0) {
				CancerStudyMetadata cancerStudyMetadata = 
					new CancerStudyMetadata(matrixRow.toArray(new String[0]));
				// get tumor type metadata
				Collection<TumorTypeMetadata> tumorTypeCollection = getTumorTypeMetadata(cancerStudyMetadata.getTumorType());
				if (!tumorTypeCollection.isEmpty()) {
					cancerStudyMetadata.setTumorTypeMetadata(tumorTypeCollection.iterator().next());
				}
				// add to return set
				toReturn.add(cancerStudyMetadata);
			}
		}

        // outta here
        return toReturn;
	}

	/**
	 * Constructs a collection of objects of the given classname from the given matrix.
	 *
	 * @param metadataMatrix ArrayList<ArrayList<String>>
	 * @param className String
	 * @return Collection<?>
	 */
	private Collection<?> getMetadataCollection(ArrayList<ArrayList<String>> metadataMatrix, String className) {

		Collection<Object> toReturn = new ArrayList<Object>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getMetadataCollection(): " + className);
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
	 * authenticate with google spreadsheet client
	 *
	 * @throws Exception
	 */
	private void login() throws Exception {

		spreadsheetService.setUserCredentials(gdataUser, gdataPassword);
	}

	/**
	 * Gets the spreadsheet.
	 *
	 * @param spreadsheetName String
	 * @returns SpreadsheetEntry
	 * @throws Exception
	 */
	private SpreadsheetEntry getSpreadsheet(String spreadsheetName) throws Exception {

		FeedURLFactory factory = FeedURLFactory.getDefault();
		SpreadsheetFeed feed = spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry entry : feed.getEntries()) {
			if (entry.getTitle().getPlainText().equals(spreadsheetName)) {
				return entry;
			}
		}
		
		// outta here
		return null;
	}

	/**
	 * Gets the worksheet feed.
	 *
	 * @param spreadsheetName String
	 * @param worksheetName String
	 * @returns WorksheetFeed
	 * @throws Exception
	 */
	private WorksheetEntry getWorksheet(String spreadsheetName, String worksheetName) throws Exception {

		// first get the spreadsheet
		SpreadsheetEntry spreadsheet = getSpreadsheet(spreadsheetName);
		if (spreadsheet != null) {
			WorksheetFeed worksheetFeed = spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
				if (worksheet.getTitle().getPlainText().equals(worksheetName)) {
					return worksheet;
				}
			}
		}

		// outta here
		return null;
	}

	/**
	 * Helper function to retrieve the given google worksheet data matrix.
	 * as a list of string lists.
	 *
	 * @param spreadsheetName String
	 * @param worksheet String
	 * @return ArrayList<ArrayList<String>>
	 */
	private ArrayList<ArrayList<String>> getWorksheetData(String spreadsheetName, String worksheetName) {

		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getWorksheetData(): " + spreadsheetName + ", " + worksheetName);
		}

		try {
			login();
			WorksheetEntry worksheet = getWorksheet(spreadsheetName, worksheetName);
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

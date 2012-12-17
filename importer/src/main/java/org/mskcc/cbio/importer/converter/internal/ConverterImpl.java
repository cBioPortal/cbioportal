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
package org.mskcc.cbio.importer.converter.internal;

// imports

import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.Converter;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.util.ClassLoader;
import org.mskcc.cbio.importer.util.DatatypeMetadataUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Class which implements the Converter interface.
 */
final class ConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(ConverterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// ref to import data
	private ImportDataRecordDAO importDataRecordDAO;

	// ref to caseids
	private CaseIDs caseIDs;

	// ref to IDMapper
	private IDMapper idMapper;

	// data sources
	private Collection<DataSourceMetadata> dataSources;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 * @param importDataRecordDAO ImportDataRecordDAO;
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public ConverterImpl(final Config config, final FileUtils fileUtils, final DatabaseUtils databaseUtils,
						 final ImportDataRecordDAO importDataRecordDAO, final CaseIDs caseIDs, final IDMapper idMapper) throws Exception {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
		this.caseIDs = caseIDs;
		this.idMapper = idMapper;
        this.dataSources = config.getDataSourceMetadata(Config.ALL_METADATA);

		// sanity check
		if (this.dataSources == null) {
			throw new IllegalArgumentException("cannot instantiate the dataSources collection.");
		}

		// initialize mapper
		initializeMapper();
	}

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void convertData(final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), portal: " + portal);
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal);
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// get datatype metadata
		Collection<DatatypeMetadata> datatypeMetadatas = config.getDatatypeMetadata();

		// iterate over all cancer studies, tumor types
		for (String cancerStudy : portalMetadata.getCancerStudies()) {

			// tumor type
			String tumorType = cancerStudy.split("_")[0];

			// iterate over all datatypes
			for (String datatype : portalMetadata.getDatatypes()) {

				// get the DatatypeMetadata object
				DatatypeMetadata datatypeMetadata = DatatypeMetadataUtils.getDatatypeMetadata(datatype, datatypeMetadatas);
				if (datatypeMetadata == null) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), unrecognized datatype: " + datatype + ", skipping");
					}
					continue;
				}

				// get DataMatrices (may be multiple in the case of methylation, median zscores, gistic-genes
				DataMatrix[] dataMatrices = getDataMatrices(portalMetadata, tumorType, datatypeMetadata);
				if (dataMatrices == null || dataMatrices.length == 0) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), error getting dataMatrices, skipping.");
					}
					continue;
				}

				// get converter and create staging file
				Object[] args = { config, fileUtils, caseIDs, idMapper };
				Converter converter =
					(Converter)ClassLoader.getInstance(datatypeMetadata.getConverterClassName(), args);
				converter.createStagingFile(portalMetadata, cancerStudy, datatypeMetadata, dataMatrices);

			}
		}
	}

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void generateCaseLists(final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("generateCaseLists()");
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal);
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// get CaseListMetadata
		Collection<CaseListMetadata> caseListMetadatas = config.getCaseListMetadata();

		// iterate over all cancer studies
		for (String cancerStudy : portalMetadata.getCancerStudies()) {
			// iterate over case lists
			for (CaseListMetadata caseListMetadata : caseListMetadatas) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), processing cancer study: " + cancerStudy + ", case list: " + caseListMetadata.getCaseListFilename());
				}
				// how many staging files are we working with?
				String[] stagingFilenames = null;
				// union (all cases)
				if (caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_UNION_DELIMITER)) {
					stagingFilenames = caseListMetadata.getStagingFilenames().split("\\" + CaseListMetadata.CASE_LIST_UNION_DELIMITER);
				}
				// intersection (like all complete or all cna & seq)
				else if (caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER)) {
					stagingFilenames = caseListMetadata.getStagingFilenames().split("\\" + CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER);
				}
				// just a single staging file
				else {
					stagingFilenames = new String[] { caseListMetadata.getStagingFilenames() };
				}
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), stagingFilenames: " + java.util.Arrays.toString(stagingFilenames));
				}
				// this is the set we will pass to writeCaseListFile
				LinkedHashSet<String> caseSet = new LinkedHashSet<String>();
				for (String stagingFilename : stagingFilenames) {
					// compute the case set
					LinkedHashSet<String> thisSet = new LinkedHashSet<String>();
					String[] stagingFileHeader = fileUtils.getStagingFileHeader(portalMetadata, cancerStudy, stagingFilename).split(CASE_DELIMITER);
					// we may not have this datatype in study
					if (stagingFileHeader.length == 0) {
						if (LOG.isInfoEnabled()) {
							LOG.info("generateCaseLists(), stagingFileHeader is empty: " + stagingFilename + ", skipping...");
						}
						continue;
					}
					// filter out column headings that are not case ids (like gene symbol or gene id)
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), filtering case ids...");
					}
					for (String caseID : stagingFileHeader) {
						if (caseIDs.isTumorCaseID(caseID)) {
							thisSet.add(caseID);
						}
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), filtering case ids complete, " + thisSet.size() + " remaining case ids...");
					}
					// if intersection 
					if (caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER)) {
						caseSet.retainAll(thisSet);
					}
					// otherwise union
					else {
						caseSet.addAll(thisSet);
					}
				}
				// write the case list file (don't make empty case lists)
				if (caseSet.size() > 0) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), calling writeCaseListFile()...");
					}
					fileUtils.writeCaseListFile(portalMetadata, cancerStudy, caseListMetadata, caseSet.toArray(new String[0]));

				}
				else if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), caseSet.size() <= 0, skipping call to writeCaseListFile()...");
				}
			}
		}
    }

	/**
	 * Creates a staging file from the given import data.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrices DataMatrix[]
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								  final DatatypeMetadata datatypeMetadata, final DataMatrix[] dataMatrices) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Helper function to initialize IDMapper.
	 *
	 * @throws Exception
	 */
	private void initializeMapper() throws Exception {

		// parse out locat
		String connectionString = (databaseUtils.getDatabaseConnectionString() +
								   databaseUtils.getGeneInformationDatabaseName() +
								   "?user=" + databaseUtils.getDatabaseUser() +
								   "&password=" + databaseUtils.getDatabasePassword());
		idMapper.initMapper(connectionString);
	}

	/**
	 * Helper function to get DataMatrix[] array.
	 *  - may return null.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param tumorType String
	 * @param datatypeMetadata DatatypeMetadata
	 * @return DataMatrix[]
	 * @throws Exception
	 */
	private DataMatrix[] getDataMatrices(final PortalMetadata portalMetadata, final String tumorType,
													 final DatatypeMetadata datatypeMetadata) throws Exception {

		// this is what we are returing
		Vector<DataMatrix> toReturn = new Vector<DataMatrix>();

		// the data type we are interested in...
		String datatype = datatypeMetadata.getDatatype();

		if (LOG.isInfoEnabled()) {
			LOG.info("getDataMatrices(), looking for all ImportDataRecord matching: " + tumorType + ":" + datatype + ".");
		}
		Collection<ImportDataRecord> allImportDataRecord = importDataRecordDAO.getImportDataRecordByTumorAndDatatype(tumorType, datatype);
		if (allImportDataRecord.size() > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("getDataMatrices(), found " + allImportDataRecord.size() + " ImportDataRecord objects matching: " + tumorType + ":" + datatype + ".");
			}
			// we will filter out ImportDataRecord with datasources not belonging to the portal
			Collection portalDataSources = portalMetadata.getDataSources();
			for (ImportDataRecord importDataRecord : allImportDataRecord) {
				if (portalDataSources.contains(importDataRecord.getDataSource())) {
					toReturn.add(fileUtils.getFileContents(portalMetadata, importDataRecord));
				}
			}
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("getDataMatrices(), cannot find any ImportDataRecord objects matching: " + tumorType + ":" + datatype + ".");
		}

		// outta here
		return toReturn.toArray(new DataMatrix[0]);
	}
}

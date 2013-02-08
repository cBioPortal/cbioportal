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
import org.mskcc.cbio.importer.Admin;
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
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.util.ClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.io.File;

/**
 * Class which implements the Converter interface.
 */
class ConverterImpl implements Converter {

	// all cases indicator
	private static final String ALL_CASES_FILENAME = "cases_all.txt";

	// our logger
	private static final Log LOG = LogFactory.getLog(ConverterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to import data
	private ImportDataRecordDAO importDataRecordDAO;

	// ref to caseids
	private CaseIDs caseIDs;

	// ref to IDMapper
	private IDMapper idMapper;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param importDataRecordDAO ImportDataRecordDAO;
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public ConverterImpl(Config config, FileUtils fileUtils, ImportDataRecordDAO importDataRecordDAO,
						 CaseIDs caseIDs, IDMapper idMapper) throws Exception {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.importDataRecordDAO = importDataRecordDAO;
		this.caseIDs = caseIDs;
		this.idMapper = idMapper;
	}

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @param runDate String
	 * @param applyOverrides Boolean
	 * @throws Exception
	 */
    @Override
	public void convertData(String portal, String runDate, Boolean applyOverrides) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), portal: " + portal);
			LOG.info("convertData(), runDate: " + runDate);
			LOG.info("convertData(), applyOverrides: " + applyOverrides);
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal).iterator().next();
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {

			// short circuit if this is a published study
			if (cancerStudyMetadata.toString().equals(CancerStudyMetadata.PUBLISHED_TCGA_STUDY_SUFFIX)) {
				if (LOG.isInfoEnabled()) {
					LOG.info("convertData(), skipping conversion of published study: " + cancerStudyMetadata);
				}
				continue;
			}

			// iterate over all datatypes
			boolean createCancerStudyMetadataFile = false;
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata(portalMetadata, cancerStudyMetadata)) {

				// get DataMatrices (may be multiple in the case of methylation, median zscores, gistic-genes
				DataMatrix[] dataMatrices = getDataMatrices(portalMetadata, cancerStudyMetadata,
															datatypeMetadata, runDate, applyOverrides);
				if (dataMatrices == null || dataMatrices.length == 0) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), no dataMatrices to process, skipping.");
					}
					continue;
				}

				// we have at least 1 data matrix, we will need to create a cancer study metadata file
				createCancerStudyMetadataFile = true;

				// get converter and create staging file
				Object[] args = { config, fileUtils, caseIDs, idMapper };
				Converter converter =
					(Converter)ClassLoader.getInstance(datatypeMetadata.getConverterClassName(), args);
				converter.createStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrices);
			}

			if (createCancerStudyMetadataFile) {
				// create cancer study metadata file
				// note - we call this again after we compute the number of cases
				fileUtils.writeCancerStudyMetadataFile(portalMetadata, cancerStudyMetadata, -1);
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
	public void generateCaseLists(String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("generateCaseLists()");
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal).iterator().next();
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// get CaseListMetadata
		Collection<CaseListMetadata> caseListMetadatas = config.getCaseListMetadata(Config.ALL);

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {
			// iterate over case lists
			for (CaseListMetadata caseListMetadata : caseListMetadatas) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), processing cancer study: " + cancerStudyMetadata + ", case list: " + caseListMetadata.getCaseListFilename());
				}
				// how many staging files are we working with?
				String[] stagingFilenames = null;
				// setup union/intersection bools
				boolean unionCaseList = 
					caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_UNION_DELIMITER);
				boolean intersectionCaseList = 
					caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER);
				// union (like all cases)
				if (unionCaseList) {
					stagingFilenames = caseListMetadata.getStagingFilenames().split("\\" + CaseListMetadata.CASE_LIST_UNION_DELIMITER);
				}
				// intersection (like complete or cna-seq)
				else if (intersectionCaseList) {
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
				// this indicates the number of staging files processed -
				// used to verify that an intersection should be written
				int numStagingFilesProcessed = 0;
				for (String stagingFilename : stagingFilenames) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), processing stagingFile: " + stagingFilename);
					}
					// compute the case set
					List<String> caseList = fileUtils.getCaseListFromStagingFile(caseIDs, portalMetadata, cancerStudyMetadata, stagingFilename);
					// we may not have this datatype in study
					if (caseList.size() == 0) {
						if (LOG.isInfoEnabled()) {
							LOG.info("generateCaseLists(), stagingFileHeader is empty: " + stagingFilename + ", skipping...");
						}
						continue;
					}
					// intersection 
					if (intersectionCaseList) {
						if (caseSet.isEmpty()) {
							caseSet.addAll(caseList);
						}
						else {
							caseSet.retainAll(caseList);
						}
					}
					// otherwise union or single staging (treat the same)
					else {
						caseSet.addAll(caseList);
					}
					++numStagingFilesProcessed;
				}
				// write the case list file (don't make empty case lists)
				if (caseSet.size() > 0) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), calling writeCaseListFile()...");
					}
					// do not write out complete cases file unless we've processed all the files required
					if (intersectionCaseList && (numStagingFilesProcessed != stagingFilenames.length)) {
						if (LOG.isInfoEnabled()) {
							LOG.info("generateCaseLists(), number of staging files processed != number staging files required for cases_complete.txt, skipping call to writeCaseListFile()...");
						}
						continue;
					}
					fileUtils.writeCaseListFile(portalMetadata, cancerStudyMetadata, caseListMetadata, caseSet.toArray(new String[0]));
				}
				else if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), caseSet.size() <= 0, skipping call to writeCaseListFile()...");
				}
				// if union, write out the cancer study metadata file
				if (caseSet.size() > 0 && caseListMetadata.getCaseListFilename().equals(ALL_CASES_FILENAME)) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), processed all cases list, we can now update cancerStudyMetadata file()...");
					}
					fileUtils.writeCancerStudyMetadataFile(portalMetadata, cancerStudyMetadata, caseSet.size());
				}
			}
		}

    }

    /**
	 * Applies overrides to the given portal using the given data source.
	 * Any datatypes within the excludes datatypes set will not have be overridden.
	 *
	 * @param portal String
	 * @param excludeDatatypes Set<String>
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("applyOverrides(), portal: " + portal);
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal).iterator().next();
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("applyOverrides(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {
			// iterate over all datatypes
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata(portalMetadata, cancerStudyMetadata)) {
				if (excludeDatatypes.contains(datatypeMetadata.getDatatype())) continue;
				// apply staging override
				String stagingFilename = datatypeMetadata.getStagingFilename().replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
				fileUtils.applyOverride(portalMetadata, cancerStudyMetadata, stagingFilename, stagingFilename);
				// apply metadata override
				if (datatypeMetadata.requiresMetafile()) {
					fileUtils.applyOverride(portalMetadata, cancerStudyMetadata,
											datatypeMetadata.getMetaFilename(), datatypeMetadata.getMetaFilename());
				}
			}
			// case lists
			fileUtils.applyOverride(portalMetadata, cancerStudyMetadata, "case_lists", "case_lists");
		}
	}

	/**
	 * Creates a staging file from the given import data.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrices DataMatrix[]
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata,
								  DatatypeMetadata datatypeMetadata, DataMatrix[] dataMatrices) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Helper function to get DataMatrix[] array.
	 *  - may return null.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @param datatypeMetadata DatatypeMetadata
	 * @param runDate String
	 * @param applyOverrides Boolean
	 * @return DataMatrix[]
	 * @throws Exception
	 */
	private DataMatrix[] getDataMatrices(PortalMetadata portalMetadata,
										 CancerStudyMetadata cancerStudyMetadata,
										 DatatypeMetadata datatypeMetadata,
										 String runDate,
										 Boolean applyOverrides) throws Exception {


		// this is what we are returing
		List<DataMatrix> toReturn = new ArrayList<DataMatrix>();

		// the data type we are interested in...
		String datatype = datatypeMetadata.getDatatype();

		if (LOG.isInfoEnabled()) {
			LOG.info("getDataMatrices(), looking for all ImportDataRecord matching: " +
					 cancerStudyMetadata.getTumorType() + ":" +
					 datatype + ":" + 
					 cancerStudyMetadata.getCenter() + ".");
		}
		Collection<ImportDataRecord> importDataRecords =
			importDataRecordDAO.getImportDataRecordByTumorTypeAndDatatypeAndCenterAndRunDate(cancerStudyMetadata.getTumorType(),
																							 datatype,
																							 cancerStudyMetadata.getCenter(),
																							 runDate);
		if (importDataRecords.size() > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("getDataMatrices(), found " + importDataRecords.size() +
						 " ImportDataRecord objects matching: " +
						 cancerStudyMetadata.getTumorType() + ":" +
						 datatype + ":" + 
						 cancerStudyMetadata.getCenter() + ".");
			}
			for (ImportDataRecord importData : importDataRecords) {
				// do we have to check for an override file?
				if (applyOverrides) {
					String dataFilename =
						importData.getDataFilename().replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG, cancerStudyMetadata.getTumorType().toUpperCase());
					File overrideFile = fileUtils.getOverrideFile(portalMetadata, cancerStudyMetadata, dataFilename);
					if (overrideFile != null) {
						if (LOG.isInfoEnabled()) {
							LOG.info("getDataMatrices(), found an override file for: " + 
									 cancerStudyMetadata.toString() + ", datatype: " + datatype + ": " + 
									 overrideFile.getCanonicalPath());
						}
						// if an override file does exist, lets replace canonical path in importData
						importData.setCanonicalPathToData(overrideFile.getCanonicalPath());
					}
				}
				DataMatrix dataMatrix = fileUtils.getFileContents(importData);
				if (dataMatrix != null) {
					toReturn.add(fileUtils.getFileContents(importData));
				}
			}
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("getDataMatrices(), cannot find any ImportDataRecord objects matching: " +
					 cancerStudyMetadata.getTumorType() + ":" +
					 datatype + ":" + 
					 cancerStudyMetadata.getCenter() + ".");
		}

		// outta here
		return toReturn.toArray(new DataMatrix[0]);
	}
}

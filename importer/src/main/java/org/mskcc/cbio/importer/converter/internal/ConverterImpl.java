/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// package
package org.mskcc.cbio.importer.converter.internal;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.util.ClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.io.File;

/**
 * Class which implements the Converter interface.
 */
class ConverterImpl implements Converter {

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
			if (!cancerStudyMetadata.isConverted()) {
				if (LOG.isInfoEnabled()) {
					LOG.info("convertData(), skipping conversion of study: " + cancerStudyMetadata);
				}
				continue;
			}

			// iterate over all datatypes
			boolean createCancerStudyMetadataFile = false;
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata(portalMetadata, cancerStudyMetadata)) {

				// get DataMatrices (may be multiple in the case of methylation, median zscores, gistic-genes
				List<DataMatrix> dataMatrices = new ArrayList<DataMatrix>();
                try {
                    dataMatrices.addAll(getDataMatrices(portalMetadata, cancerStudyMetadata, datatypeMetadata, runDate, applyOverrides));
                } catch (Exception e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.error("convertData(), exception:\n" + e.getMessage());
                    }
                    continue;
                }
				if (dataMatrices.isEmpty()) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), no dataMatrices to process, skipping.");
					}
					continue;
				}

				// we have at least 1 data matrix, we will need to create a cancer study metadata file
				createCancerStudyMetadataFile = true;

				// get converter and create staging file
				Object[] args = { config, fileUtils, caseIDs, idMapper };
				Converter converter;
                                try {
					converter = (Converter)ClassLoader.getInstance(datatypeMetadata.getConverterClassName(), args);
				} catch (ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                    continue;
                                }
                                converter.createStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrices.toArray(new DataMatrix[0]));
			}

			if (createCancerStudyMetadataFile) {
				// create cancer study metadata file
				// note - we call this again after we compute the number of cases
				fileUtils.writeCancerStudyMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, -1);
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

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {
			fileUtils.generateCaseLists(true, true, portalMetadata.getStagingDirectory(), cancerStudyMetadata);
		}
    }

    /**
	 * Applies overrides to the given portal using the given data source.
	 * Any datatypes within the excludes datatypes set will not have be overridden.
	 *
	 * @param portal String
	 * @param excludeDatatypes Set<String>
	 * @param applyCaseLists boolean
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes, boolean applyCaseLists) throws Exception {

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
				fileUtils.applyOverride(portalMetadata.getOverrideDirectory(), portalMetadata.getStagingDirectory(),
                                        cancerStudyMetadata, stagingFilename, stagingFilename);
				// apply metadata override
				if (datatypeMetadata.requiresMetafile()) {
					fileUtils.applyOverride(portalMetadata.getOverrideDirectory(), portalMetadata.getStagingDirectory(),
                                            cancerStudyMetadata, datatypeMetadata.getMetaFilename(), datatypeMetadata.getMetaFilename());
				}
			}
			// case lists
			if (applyCaseLists) {
				fileUtils.applyOverride(portalMetadata.getOverrideDirectory(), portalMetadata.getStagingDirectory(),
                                        cancerStudyMetadata, "case_lists", "case_lists");
			}
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

	private List<DataMatrix> getDataMatrices(PortalMetadata portalMetadata,
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
		List<ImportDataRecord> importDataRecords =
			importDataRecordDAO.getImportDataRecordByTumorTypeAndDatatypeAndCenterAndRunDate(cancerStudyMetadata.getTumorType(),
																							 datatype,
																							 cancerStudyMetadata.getCenter(),
																							 datatype.contains("clinical") ?
                                                                                             Fetcher.LATEST_RUN_INDICATOR : runDate);
		if (importDataRecords.size() > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("getDataMatrices(), found " + importDataRecords.size() +
						 " ImportDataRecord objects matching: " +
						 cancerStudyMetadata.getTumorType() + ":" +
						 datatype + ":" + 
						 cancerStudyMetadata.getCenter() + ".");
			}
			// if methylation data, make sure correlation file comes before methylation data
			if (datatype.contains("methylation")) {
                            if (importDataRecords.size() < 2) {
                                throw new Exception("Require two data sets for converting methylation data.");
                            } else {
				Collections.sort(importDataRecords, new ImportDataRecordComparator());
                            }
			}
			DataMatrix methylationCorrelation = null;
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
				List<DataMatrix> dataMatrices = fileUtils.getDataMatrices(importData, methylationCorrelation);
				if (!dataMatrices.isEmpty()) {
					if (importData.getDataFilename().contains(DatatypeMetadata.CORRELATE_METHYL_FILE_ID)) {
                        assert dataMatrices.size() == 1;
						methylationCorrelation = dataMatrices.get(0);
					}
					toReturn.addAll(dataMatrices);
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
		return toReturn;
	}
}

class ImportDataRecordComparator implements Comparator {
	public int compare (Object o, Object o1) {
		ImportDataRecord record0 = (ImportDataRecord)o;
		ImportDataRecord record1 = (ImportDataRecord)o1;
		return (record1.getDataFilename().contains(DatatypeMetadata.CORRELATE_METHYL_FILE_ID)) ? 1 : -1;
	}
}

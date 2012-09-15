// package
package org.mskcc.cbio.importer.converter.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Converter;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.StagingFileFactory;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.dao.ImportDataDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JTable;

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

	// ref to import data
	private ImportDataDAO importDataDAO;

	/**
	 * Constructor.
     *
     * Takes a Config reference.
	 * Takes a FileUtils reference.
	 * Takes a ImportDataDAO reference.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param importDataDAO ImportDataDAO;
	 */
	public ConverterImpl(final Config config, final FileUtils fileUtils, final ImportDataDAO importDataDAO) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.importDataDAO = importDataDAO;
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

        // iterate over all import data objects
        for (ImportData importData : importDataDAO.getImportData()) {

            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), determining if importData object belongs in portal: " +
                         importData.getTumorType() + "/" + importData.getDatatype());
            }

            // does this cancer study / datatype belong in this portal?
            if (!belongsInPortal(portalMetadata, importData)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("convertData(), importData does not belongs in portal, skipping");
                }
                continue;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("convertData(), importData does belong, getting content");
            }

            // get data to process as JTable
            JTable table = fileUtils.getFileContents(portalMetadata, importData);

            // get converter
            
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
    }

    /**
     * Helper function - determines if the given ImportData
     * belongs in the given portal.
     *
     * @param portalMetadata PortalMetadata
     * @param importData ImportData
     * @return Boolean
     */
    private Boolean belongsInPortal(final PortalMetadata portalMetadata, final ImportData importData) {
        
        // check cancer studies
        for (String cancerStudy : portalMetadata.getCancerStudies()) {
            if (cancerStudy.contains(importData.getTumorType().toLowerCase())) {
                for (String datatype : portalMetadata.getDatatypes()) {
                    if (datatype.contains(importData.getDatatype().toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        // outta here
        return false;
    }
}

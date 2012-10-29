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
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.ImportDataMatrix;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.dao.ImportDataDAO;
import org.mskcc.cbio.importer.util.ClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

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
	private ImportDataDAO importDataDAO;

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
	 * @param importDataDAO ImportDataDAO;
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public ConverterImpl(final Config config, final FileUtils fileUtils, final DatabaseUtils databaseUtils,
						 final ImportDataDAO importDataDAO, final CaseIDs caseIDs, final IDMapper idMapper) throws Exception {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataDAO = importDataDAO;
		this.caseIDs = caseIDs;
		this.idMapper = idMapper;
        this.dataSources = config.getDataSourceMetadata("all");

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
				DatatypeMetadata datatypeMetadata = getDatatypeMetadata(datatype, datatypeMetadatas);
				if (datatypeMetadata == null) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), unrecognized datatype: " + datatype + ", skipping");
					}
					continue;
				}

				// get ImportDataMatrices (may be multiple in the case of methylation, median zscores, gistic-genes
				ImportDataMatrix[] importDataMatrices = getImportDataMatrices(portalMetadata, tumorType, datatypeMetadata);
				if (importDataMatrices == null) {
					if (LOG.isInfoEnabled()) {
						LOG.info("convertData(), error getting importDataMatrices, skipping.");
					}
					continue;
				}

				// get converter and create staging file
				Object[] args = { config, fileUtils, caseIDs, idMapper };
				Converter converter =
					(Converter)ClassLoader.getInstance(datatypeMetadata.getConverterClassName(), args);
				converter.createStagingFile(portalMetadata, cancerStudy, datatypeMetadata, importDataMatrices);
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
    }

	/**
	 * Creates a staging file from the given import data.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param importDataMatrices ImportDataMatrix[]
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								  final DatatypeMetadata datatypeMetadata, final ImportDataMatrix[] importDataMatrices) throws Exception {
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
	 * Helper function to get datatype metadata object for given datatype.
	 *
	 * @param datatype String
	 * @param datatypeMetadata Collection<datatypeMetadata>
	 * @return DatatypeMetadata
	 */
	private DatatypeMetadata getDatatypeMetadata(final String datatype,
												 final Collection<DatatypeMetadata> datatypeMetadata) {
		
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
            if (dtMetadata.getDatatype().toLowerCase().equals(datatype.toLowerCase())) {
				return dtMetadata;
            }
		}

		// outta here
		return null;
	}

	/**
	 * Helper function to get ImportDataMatrix[] array.
	 *  - may return null.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param tumorType String
	 * @param datatypeMetadata DatatypeMetadata
	 * @return ImpordDataMatrix[]
	 * @throws Exception
	 */
	private ImportDataMatrix[] getImportDataMatrices(final PortalMetadata portalMetadata, final String tumorType,
													 final DatatypeMetadata datatypeMetadata) throws Exception {

		ImportDataMatrix[] toReturn = null;

		String datatype = datatypeMetadata.getDatatype();
		if (!datatypeMetadata.isComputable()) {
			toReturn = new ImportDataMatrix[] { getImportDataMatrix(portalMetadata, tumorType, datatype) };
		}
		// computable, we need to parse 
		else {
			String[] computableDatatypes = datatypeMetadata.getDatatypeDependencies();
			toReturn = new ImportDataMatrix[computableDatatypes.length];
			for (int lc = 0; lc < computableDatatypes.length; lc++) {
				String computableDatatype = computableDatatypes[lc];
				toReturn[lc] = getImportDataMatrix(portalMetadata, tumorType, computableDatatype);
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Helper function to get an ImportDataMatrix for the given tumorType/datatype combination.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param tumorType String
	 * @param datatype String
	 * @return ImportDataMatrix
	 * @throws Exception
	 */
	private ImportDataMatrix getImportDataMatrix(final PortalMetadata portalMetadata, final String tumorType,
												 final String datatype) throws Exception {

		ImportDataMatrix toReturn = null;

		if (LOG.isInfoEnabled()) {
			LOG.info("getImportDataMatrix(), looking for ImportData matching: " + tumorType + ":" + datatype + ".");
		}
		ImportData importData = importDataDAO.getImportDataByTumorAndDatatype(tumorType, datatype);
		if (importData != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("getImportDataMatrix(), found ImportData matching: " + tumorType + ":" + datatype + ".");
			}
			toReturn = fileUtils.getFileContents(portalMetadata, importData);
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("getImportDataMatrix(), cannot find ImportData matching: " + tumorType + ":" + datatype);
		}

		// outta here
		return toReturn;
	}
}

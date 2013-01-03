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
import org.mskcc.cbio.importer.util.MapperUtil;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Arrays;

/**
 * Class which implements the Converter interface.
 */
public class CNAConverterImpl implements Converter {

	private static final String GENE_ID_COLUMN_HEADER_NAME = "Locus ID";
	private static final String GENE_SYMBOL_COLUMN_HEADER_NAME = "Gene Symbol";

	// our logger
	private static final Log LOG = LogFactory.getLog(CNAConverterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to caseids
	private CaseIDs caseIDs;

	// ref to IDMapper
	private IDMapper idMapper;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public CNAConverterImpl(Config config, FileUtils fileUtils,
							CaseIDs caseIDs, IDMapper idMapper) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.caseIDs = caseIDs;
		this.idMapper = idMapper;
	}

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @param applyOverrides Boolean
	 * @throws Exception
	 */
    @Override
	public void convertData(String portal, Boolean applyOverrides) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void generateCaseLists(String portal) throws Exception {
		throw new UnsupportedOperationException();
    }

    /**
	 * Applies overrides to the given portal using the given data source.
	 *
	 * @param portal String
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal) throws Exception {
		throw new UnsupportedOperationException();
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

		// sanity check
		if (dataMatrices.length != 1) {
			throw new IllegalArgumentException("dataMatrices.length != 1, aborting...");
		}
		DataMatrix dataMatrix = dataMatrices[0];

		// perform gene mapping, remove records as needed
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), calling MapperUtil.mapGeneSymbolToID()...");
		}
		MapperUtil.mapGeneSymbolToID(dataMatrix, idMapper,
									 GENE_ID_COLUMN_HEADER_NAME, GENE_SYMBOL_COLUMN_HEADER_NAME);

		// rename columns
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), renaming columns");
		}
		dataMatrix.renameColumn(GENE_SYMBOL_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		dataMatrix.renameColumn(GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setGeneIDColumnHeading(Converter.GENE_ID_COLUMN_HEADER_NAME);

		// convert case ids
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), filtering & converting case ids");
		}
		String[] columnsToIgnore = { Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, Converter.GENE_ID_COLUMN_HEADER_NAME }; // drop Cytoband
		dataMatrix.convertCaseIDs(Arrays.asList(columnsToIgnore));

		// ensure the first two columns are symbol, id respectively
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), sorting column headers");
		}
		List<String> columnHeaders = dataMatrix.getColumnHeaders();
		columnHeaders.remove(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.add(0, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.remove(Converter.GENE_ID_COLUMN_HEADER_NAME);
		columnHeaders.add(1, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setColumnOrder(columnHeaders);

		// we need to write out the file
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrix);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}
}

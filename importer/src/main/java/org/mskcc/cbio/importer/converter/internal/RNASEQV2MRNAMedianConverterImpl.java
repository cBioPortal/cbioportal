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
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Class which implements the Converter interface for processing rna-seq (v2) RSEM files.
 */
public class RNASEQV2MRNAMedianConverterImpl implements Converter {

	protected static final String HYBRIDIZATION_REF_COLUMN_HEADER_NAME = "Hybridization REF";

	// our logger
	private static final Log LOG = LogFactory.getLog(RNASEQV2MRNAMedianConverterImpl.class);

	// ref to configuration
	protected Config config;

	// ref to file utils
	protected FileUtils fileUtils;

	// ref to caseids
	protected CaseIDs caseIDs;

	// ref to IDMapper
	protected IDMapper idMapper;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public RNASEQV2MRNAMedianConverterImpl(Config config, FileUtils fileUtils,
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
	 * @param runDate String
	 * @param applyOverrides Boolean
	 * @throws Exception
	 */
    @Override
	public void convertData(String portal, String runDate, Boolean applyOverrides) throws Exception {
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

		// row one (zero offset) in file is another header:
		// (gene, normalized count, normalized count, ...)
		dataMatrix.ignoreRow(0, true); // row data starts at 0

		// rna seq data files has combination gene_symbol|id
		// replace the combination with gene_symbol only
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), cleaning up Hybridization REF column...");
		}
		List<String> pairs = dataMatrix.getColumnData(HYBRIDIZATION_REF_COLUMN_HEADER_NAME).get(0);
		for (int lc = 0; lc < pairs.size(); lc++) {
			String[] parts = pairs.get(lc).trim().split("\\|");
			if (parts.length == 2) {
				if (LOG.isInfoEnabled()) {
					LOG.info("setting element: " + Arrays.asList(parts) + ", to: " + parts[1]);
				}
				pairs.set(lc, parts[1]);
			}
		}

		// add gene symbol column, rename gene id col
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), adding & renaming columns");
		}
		dataMatrix.addColumn(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, new ArrayList<String>());
		dataMatrix.renameColumn(HYBRIDIZATION_REF_COLUMN_HEADER_NAME, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setGeneIDColumnHeading(Converter.GENE_ID_COLUMN_HEADER_NAME);

		// perform gene mapping, remove records as needed
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), calling MapperUtil.mapDataToGeneID()...");
		}
		MapperUtil.mapGeneIDToSymbol(dataMatrix, idMapper,
									 Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);

		// convert case ids
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), filtering & converting case ids");
		}
		String[] columnsToIgnore = { Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME };
		dataMatrix.convertCaseIDs(Arrays.asList(columnsToIgnore));

		// ensure the first two columns are symbol, id respectively
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), sorting column headers");
		}
		List<String> headers = dataMatrix.getColumnHeaders();
		headers.remove(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		headers.add(0, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		headers.remove(Converter.GENE_ID_COLUMN_HEADER_NAME);
		headers.add(1, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setColumnOrder(headers);

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

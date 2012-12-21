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

import java.util.Arrays;
import java.util.Vector;

/**
 * Class which implements the Converter interface.
 */
public final class RNASEQMRNAMedianConverterImpl implements Converter {

	private static final String HYBRIDIZATION_REF_COLUMN_HEADER_NAME = "Hybridization REF";

	// our logger
	private static final Log LOG = LogFactory.getLog(RNASEQMRNAMedianConverterImpl.class);

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
	public RNASEQMRNAMedianConverterImpl(final Config config, final FileUtils fileUtils,
										 final CaseIDs caseIDs, final IDMapper idMapper) {

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
	 * @throws Exception
	 */
    @Override
	public void convertData(final String portal) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void generateCaseLists(final String portal) throws Exception {
		throw new UnsupportedOperationException();
    }

	/**
	 * Applies overrides to the given portal using the given data source.
	 *
     * @param portal String
	 * @param dataSource String
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(final String portal, final String dataSource) throws Exception {
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
	public void createStagingFile(final PortalMetadata portalMetadata, final CancerStudyMetadata cancerStudyMetadata,
								  final DatatypeMetadata datatypeMetadata, final DataMatrix[] dataMatrices) throws Exception {

		// sanity check
		if (dataMatrices.length != 1) {
			throw new IllegalArgumentException("dataMatrices.length != 1, aborting...");
		}
		DataMatrix dataMatrix = dataMatrices[0];

		// rnaseq files have 3 columns per sample (first column is Hybridization REF).
		// discard first & second columns and take third - RPKM
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), removing  and keepng RPKM column per sample");
		}
		String previousHeader = "";
		Vector<String> columnHeaders = dataMatrix.getColumnHeaders();
		for (int lc = columnHeaders.size()-1; lc >= 0; lc--) {
			String columnHeader = columnHeaders.get(lc);
			if (columnHeader.equals(previousHeader)) {
				dataMatrix.ignoreColumn(lc, true);
			}
			else {
				previousHeader = columnHeader;
			}
		}

		// row one (zero offset) in file is another header:
		// (gene, raw_counts, median_length_normalized, RPKM, raw_counts,  median_length_normalized, RPKM, raw_counts, ...)
		dataMatrix.ignoreRow(0, true); // row data starts at 0

		// rna seq data files has combination gene_symbol|id
		// replace the combination with gene_symbol only
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), cleaning up Hybridization REF column...");
		}
		Vector<String> pairs = dataMatrix.getColumnData(HYBRIDIZATION_REF_COLUMN_HEADER_NAME).get(0);
		for (int lc = 0; lc < pairs.size(); lc++) {
			String[] parts = pairs.get(lc).trim().split("\\|");
			if (parts.length == 2) {
				if (LOG.isInfoEnabled()) {
					LOG.info("setting element: " + Arrays.asList(parts) + ", to: " + parts[1]);
				}
				pairs.setElementAt(parts[1], lc);
			}
		}

		// add gene symbol column, rename gene id col
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), adding & renaming columns");
		}
		dataMatrix.addColumn(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, new Vector<String>());
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
		Vector<String> headers = dataMatrix.getColumnHeaders();
		headers.removeElement(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		headers.insertElementAt(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, 0);
		headers.removeElement(Converter.GENE_ID_COLUMN_HEADER_NAME);
		headers.insertElementAt(Converter.GENE_ID_COLUMN_HEADER_NAME, 1);
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

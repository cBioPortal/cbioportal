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
import org.mskcc.cbio.importer.model.DataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Vector;

/**
 * Class which implements the Converter interface.
 */
public final class GisticGenesConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(GisticGenesConverterImpl.class);

	// statics for row identifiers in *_genes.conf_99.txt
	private static final String GENES_CONF_CYTOBAND_ROW_HEADER_NAME = "cytoband";
	private static final String GENES_CONF_Q_VALUE_ROW_HEADER_NAME = "q value";
	private static final String GENES_CONF_GENES_IN_WIDE_PEAK_ROW_HEADER_NAME = "genes in wide peak";
	// statics for column identifiers in table_*.conf_99.txt
	private static final String TABLE_CONF_CYTOBAND_COLUMN_HEADER_NAME = "cytoband";
	private static final String TABLE_CONF_Q_VALUE_COLUMN_HEADER_NAME = "q_value";
	private static final String TABLE_CONF_INDEX_COLUMN_HEADER_NAME = "index";
	private static final String TABLE_CONF_GENES_IN_REGION_HEADER_NAME = "genes_in_region";
	private static final String TABLE_CONF_GENES_IN_REGION_DELIMITER = ",";
	// our GeneConfMap value delimiter
	private static final String GENE_CONF_MAP_VALUE_DELIMITER = ":";

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
	public GisticGenesConverterImpl(final Config config, final FileUtils fileUtils,
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
	public void generateCaseLists(final String portal) throws Exception {}

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

		// sanity check
		if (dataMatrices.length != 2) {
			throw new IllegalArgumentException("dataMatrices.length != 2, aborting...");
		}

		// figure out which matrix is *_genes.conf_99.txt
		// and which matrix is table_*.conf_90.txt
		DataMatrix dataMatrixGenesConf = null;
		DataMatrix dataMatrixTableConf = null;
		if (dataMatrices[0].getColumnHeaders().firstElement().equalsIgnoreCase(GENES_CONF_CYTOBAND_ROW_HEADER_NAME) &&
			dataMatrices[1].getColumnHeaders().firstElement().equalsIgnoreCase(TABLE_CONF_INDEX_COLUMN_HEADER_NAME)) {
			dataMatrixGenesConf = dataMatrices[0];
			dataMatrixTableConf = dataMatrices[1];
		}
		else if (dataMatrices[0].getColumnHeaders().firstElement().equalsIgnoreCase(TABLE_CONF_INDEX_COLUMN_HEADER_NAME) && 
				 dataMatrices[1].getColumnHeaders().firstElement().equalsIgnoreCase(GENES_CONF_CYTOBAND_ROW_HEADER_NAME)) {
			dataMatrixTableConf = dataMatrices[0];
			dataMatrixGenesConf = dataMatrices[1];
		}
		else {
			throw new IllegalArgumentException("Cannot determine *_genes.conf_99.txt & table_*.conf_90.txt matrices, aborting...");
		}

		// process *_genes.conf_99.txt file first - get map, geneset is key, cytoband:q-value is value
		Map<String, String> geneConfMap = getGenesConfMap(dataMatrixGenesConf);

		// now that we have *_genes.conf_99.txt map, we can process table_*.conf_90.txt
		// - add cytoband and q_value columns to table_*.conf_90.txt
		Vector<String> cytobandColumnData = new Vector<String>();
		Vector<String> qValueColumnData = new Vector<String>();
		for (String geneSet : dataMatrixTableConf.getColumnData(TABLE_CONF_GENES_IN_REGION_HEADER_NAME).get(0)) {
			if (geneSet.endsWith(TABLE_CONF_GENES_IN_REGION_DELIMITER)) {
				geneSet = geneSet.substring(0, geneSet.length()-1);
			}
			if (geneConfMap.containsKey(geneSet)) {
				String[] cytobandQValuePair = geneConfMap.get(geneSet).split(GENE_CONF_MAP_VALUE_DELIMITER);
				cytobandColumnData.add(cytobandQValuePair[0]);
				qValueColumnData.add(cytobandQValuePair[1]);
			}
			else if (LOG.isInfoEnabled()) {
				LOG.info("createStagingFile(), cannot find GeneConfMap key: " + geneSet);
			}
		}
		dataMatrixTableConf.addColumn(TABLE_CONF_CYTOBAND_COLUMN_HEADER_NAME, cytobandColumnData);
		dataMatrixTableConf.addColumn(TABLE_CONF_Q_VALUE_COLUMN_HEADER_NAME, qValueColumnData);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeStagingFile(portalMetadata, cancerStudy, datatypeMetadata, dataMatrixTableConf);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}

	/**
	 * Helper function to process *_genes.conf_99.txt files
	 *
	 * @param dataMatrix DataMatrix
	 * @return Map<String, String>
	 * @throws Exception
	 */
	private Map<String,String> getGenesConfMap(final DataMatrix dataMatrixGenesConf) throws Exception {

		// the map to return
		Map<String,String> toReturn = new HashMap<String,String>();

		// the column headers are actually the cytobands
		Vector<String> geneConfColumnHeaders = dataMatrixGenesConf.getColumnHeaders();
		int cytobandColumnIndex = geneConfColumnHeaders.indexOf(GENES_CONF_CYTOBAND_ROW_HEADER_NAME);
		// sanity check - "cytoband" label should be first column header
		if (cytobandColumnIndex != 0) {
			throw new IllegalArgumentException("Corrupt *_genes.conf_99.txt, aborting...");
		}

		// get row headers and determine which row contains qValues and which contains genes-in-wide-peak
		Vector<String> geneConfRowHeaders = dataMatrixGenesConf.getColumnData(GENES_CONF_CYTOBAND_ROW_HEADER_NAME).get(0);
		int qValueRowIndex = geneConfRowHeaders.indexOf(GENES_CONF_Q_VALUE_ROW_HEADER_NAME);
		int genesInWidePeakStartRowIndex = geneConfRowHeaders.indexOf(GENES_CONF_GENES_IN_WIDE_PEAK_ROW_HEADER_NAME);
		// sanity check
		if (qValueRowIndex == -1 || genesInWidePeakStartRowIndex == -1) {
			throw new IllegalArgumentException("Corrupt *_genes.conf_99.txt, aborting...");
		}

		// interate over all cytobands and put proper key:value pairs into our return map
		for (int lc = 1; lc < geneConfColumnHeaders.size(); lc++) {
			// the cytoband is the column header
			String cytoband = geneConfColumnHeaders.get(lc);
			// the column of data for this cytoband
			Vector<String> columnDataForThisCytoband = dataMatrixGenesConf.getColumnData(lc);
			// get the q-value, its in the column indexed by lc, and row qValueRowIndex
			String qValue = columnDataForThisCytoband.elementAt(qValueRowIndex);
			// the wide peak gene set
			StringBuilder genesInWidePeak = new StringBuilder();
			for (String gene : columnDataForThisCytoband.subList(genesInWidePeakStartRowIndex, columnDataForThisCytoband.size())) {
				if (gene.length() > 0) {
					genesInWidePeak.append(gene + TABLE_CONF_GENES_IN_REGION_DELIMITER);
				}
			}
			String genesInWidePeakKey = genesInWidePeak.substring(0, genesInWidePeak.length()-1);
			String genesInWidePeakValue = cytoband + GENE_CONF_MAP_VALUE_DELIMITER + qValue;
			if (LOG.isDebugEnabled()) {
				LOG.debug("getGenesConfMap(), adding the following key-value to genesConfMap:");
				LOG.debug(genesInWidePeakKey + "->" + genesInWidePeakValue);
			}
			toReturn.put(genesInWidePeakKey, genesInWidePeakValue);
		}

		// outta here
		return toReturn;
	}
}


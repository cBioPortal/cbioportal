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
package org.mskcc.cbio.importer.converter.internal;

import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.util.MapperUtil;
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * Class which implements the Converter interface.
 */
public class GisticGenesConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(GisticGenesConverterImpl.class);

	// statics for row identifiers in *_genes.conf_99.txt
	private static final String GENES_CONF_CYTOBAND_ROW_HEADER_NAME = "cytoband";
	private static final String GENES_CONF_Q_VALUE_ROW_HEADER_NAME = "q value";
	private static final String GENES_CONF_GENES_IN_WIDE_PEAK_ROW_HEADER_NAME = "genes in wide peak";
	// statics for column identifiers in table_*.conf_99.txt
	private static final String TABLE_CONF_AMP_COLUMN_HEADER_NAME = "amp";
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
	public GisticGenesConverterImpl(Config config, FileUtils fileUtils,
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
	 * Any datatypes within the excludes datatypes set will not have be overridden.
	 *
	 * @param portal String
	 * @param excludeDatatypes Set<String>
	 * @param applyCaseLists boolean
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes, boolean applyCaseLists) throws Exception {
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
		if (dataMatrices.length != 2) {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), dataMatrices.length != 2, aborting...");
			}
			return;
		}

		// figure out which matrix is *_genes.conf_99.txt
		// and which matrix is table_*.conf_90.txt
		DataMatrix dataMatrixGenesConf = null;
		DataMatrix dataMatrixTableConf = null;
		if (dataMatrices[0].getColumnHeaders().get(0).equalsIgnoreCase(GENES_CONF_CYTOBAND_ROW_HEADER_NAME) &&
			dataMatrices[1].getColumnHeaders().get(0).equalsIgnoreCase(TABLE_CONF_INDEX_COLUMN_HEADER_NAME)) {
			dataMatrixGenesConf = dataMatrices[0];
			dataMatrixTableConf = dataMatrices[1];
		}
		else if (dataMatrices[0].getColumnHeaders().get(0).equalsIgnoreCase(TABLE_CONF_INDEX_COLUMN_HEADER_NAME) && 
				 dataMatrices[1].getColumnHeaders().get(0).equalsIgnoreCase(GENES_CONF_CYTOBAND_ROW_HEADER_NAME)) {
			dataMatrixTableConf = dataMatrices[0];
			dataMatrixGenesConf = dataMatrices[1];
		}
		else {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), cannot determine *_genes.conf_99.txt & table_*.conf_90.txt matrices, aborting...");
			}
			return;
		}

		// process *_genes.conf_99.txt file first - get map, geneset is key, cytoband:q-value is value
		Map<String, String> geneConfMap = null;
		try {
			geneConfMap = getGenesConfMap(dataMatrixGenesConf);
		}
		catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage());
			}
			return;
		}

		// now that we have *_genes.conf_99.txt map, we can process table_*.conf_90.txt
		// - add amp, cytoband and q_value columns to table_*.conf_90.txt
		List<String> amp = new ArrayList<String>();
		List<String> cytobandColumnData = new ArrayList<String>();
		List<String> qValueColumnData = new ArrayList<String>();
		String ampFlag = (datatypeMetadata.getDatatype().contains("amp") || datatypeMetadata.getDatatype().contains("AMP")) ? "1" : "0";
		for (String geneSet : dataMatrixTableConf.getColumnData(TABLE_CONF_GENES_IN_REGION_HEADER_NAME).get(0)) {
			if (geneSet.endsWith(TABLE_CONF_GENES_IN_REGION_DELIMITER)) {
				geneSet = geneSet.substring(0, geneSet.length()-1);
			}
			if (geneConfMap.containsKey(geneSet)) {
				String[] cytobandQValuePair = geneConfMap.get(geneSet).split(GENE_CONF_MAP_VALUE_DELIMITER);
				cytobandColumnData.add(cytobandQValuePair[0]);
				qValueColumnData.add(cytobandQValuePair[1]);
				amp.add(ampFlag);
			}
			else if (LOG.isInfoEnabled()) {
				LOG.info("createStagingFile(), cannot find GeneConfMap key: " + geneSet);
			}
		}
		dataMatrixTableConf.addColumn(TABLE_CONF_AMP_COLUMN_HEADER_NAME, amp);
		dataMatrixTableConf.addColumn(TABLE_CONF_CYTOBAND_COLUMN_HEADER_NAME, cytobandColumnData);
		dataMatrixTableConf.addColumn(TABLE_CONF_Q_VALUE_COLUMN_HEADER_NAME, qValueColumnData);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeStagingFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrixTableConf);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}

		if (datatypeMetadata.requiresMetafile()){
			if (LOG.isInfoEnabled()) {
				LOG.info("createStagingFile(), writing metadata file.");
			}
			fileUtils.writeMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrixTableConf);
		}	
	}

	/**
	 * Helper function to process *_genes.conf_99.txt files
	 *
	 * @param dataMatrix DataMatrix
	 * @return Map<String, String>
	 * @throws Exception
	 */
	private Map<String,String> getGenesConfMap(DataMatrix dataMatrixGenesConf) throws Exception {

		// the map to return
		Map<String,String> toReturn = new HashMap<String,String>();

		// the column headers are actually the cytobands
		List<String> geneConfColumnHeaders = dataMatrixGenesConf.getColumnHeaders();
		int cytobandColumnIndex = geneConfColumnHeaders.indexOf(GENES_CONF_CYTOBAND_ROW_HEADER_NAME);
		// sanity check - "cytoband" label should be first column header
		if (cytobandColumnIndex != 0) {
			throw new IllegalArgumentException("Corrupt *_genes.conf_99.txt, aborting...");
		}

		// get row headers and determine which row contains qValues and which contains genes-in-wide-peak
		List<String> geneConfRowHeaders = dataMatrixGenesConf.getColumnData(GENES_CONF_CYTOBAND_ROW_HEADER_NAME).get(0);
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
			List<String> columnDataForThisCytoband = dataMatrixGenesConf.getColumnData(lc);
			// get the q-value, its in the column indexed by lc, and row qValueRowIndex
			String qValue = columnDataForThisCytoband.get(qValueRowIndex);
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


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
public class MethylationConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(MethylationConverterImpl.class);

	// statics for column identifiers in correlate - methylation file
	private static final String CORRELATE_GENE_COLUMN_HEADER_NAME = "Gene";
	public static final String CORRELATE_METH_PROBE_COLUMN_HEADER_NAME = "Meth_Probe"; // used in FileUtilsImpl
	// in runs up to 7/25/2012, Corr_Spearman appears, from 10/24/2012 Corr_Coeff exists
	private static final String CORRELATE_SPEARMAN_COLUMN_HEADER_NAME = "Corr_Spearman";
	private static final String CORRELATE_COEFF_COLUMN_HEADER_NAME = "Corr_Coeff"; 

	// statics for column identifiers in <CANCER>.methylation__humanmethylation[27|450] file
	private static final String METHYLATION_CHROMOSOME_COLUMN_HEADER_NAME = "Chromosome";
	private static final String METHYLATION_GENE_SYMBOL_COLUMN_HEADER_NAME = "Gene_Symbol";
	private static final String METHYLATION_HYBRIDIZATION_REF_COLUMN_HEADER_NAME = "Hybridization REF";
	private static final String METHYLATION_GENOMIC_COORDINATE_COLUMN_HEADER_NAME = "Genomic_Coordinate";

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
	public MethylationConverterImpl(Config config, FileUtils fileUtils,
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

		// determine which matrix is methylation data
		// and which matrix is the correlation data
		DataMatrix dataMatrixMethylationData = null;
		DataMatrix dataMatrixCorrelationData = null;
		if (dataMatrices[0].getColumnHeaders().get(0).equalsIgnoreCase(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME) &&
			dataMatrices[1].getColumnHeaders().get(0).equalsIgnoreCase(METHYLATION_HYBRIDIZATION_REF_COLUMN_HEADER_NAME)) {
			dataMatrixCorrelationData = dataMatrices[0];
			dataMatrixMethylationData = dataMatrices[1];
		}
		else if (dataMatrices[0].getColumnHeaders().get(0).equalsIgnoreCase(METHYLATION_HYBRIDIZATION_REF_COLUMN_HEADER_NAME) &&
				 dataMatrices[1].getColumnHeaders().get(0).equalsIgnoreCase(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME)) {
			dataMatrixMethylationData = dataMatrices[0];
			dataMatrixCorrelationData = dataMatrices[1];
		}
		else {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), cannot determine correlation & methylation data matrices, aborting...");
			}
			return;
		}

		// get probe with lowest Spearman correlation for each gene in dataMatrixCorrelationData
		// (this works whether or not dataMatrixCorrelationData is sorted by correlation value)
		HashMap<String,String[]> lowestCorrelationMap = new HashMap<String,String[]>();
		List<String> genes = null; List<String>methProbes = null; List<String>corrSpearman = null;
		if (dataMatrixCorrelationData.getColumnData(CORRELATE_GENE_COLUMN_HEADER_NAME).size() > 0) {
			genes = dataMatrixCorrelationData.getColumnData(CORRELATE_GENE_COLUMN_HEADER_NAME).get(0);
		}
		if (dataMatrixCorrelationData.getColumnData(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME).size() > 0) {
			methProbes = dataMatrixCorrelationData.getColumnData(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME).get(0);
		}
		if (dataMatrixCorrelationData.getColumnData(CORRELATE_SPEARMAN_COLUMN_HEADER_NAME).size() > 0) {
			corrSpearman = dataMatrixCorrelationData.getColumnData(CORRELATE_SPEARMAN_COLUMN_HEADER_NAME).get(0);
		}
		else if (dataMatrixCorrelationData.getColumnData(CORRELATE_COEFF_COLUMN_HEADER_NAME).size() > 0) {
			corrSpearman = dataMatrixCorrelationData.getColumnData(CORRELATE_COEFF_COLUMN_HEADER_NAME).get(0);
		}
		// sanity checks
		if (genes == null || methProbes == null || corrSpearman == null ||
			(genes.size() != methProbes.size() && methProbes.size() != corrSpearman.size())) {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), errors processing correlation file, aborting...");
			}
			return;
		}
		// iterate over all records
		for (int lc = 0; lc < genes.size(); lc++) {
			String gene = genes.get(lc);
			String methProbe = methProbes.get(lc);
			String cSpearman = corrSpearman.get(lc);
			if (lowestCorrelationMap.containsKey(gene)) {
				if (Double.valueOf(cSpearman) < Double.valueOf(lowestCorrelationMap.get(gene)[1])) {
					String[] value = { methProbe, cSpearman };
					lowestCorrelationMap.put(gene, value);
				}
			}
			else {
				String[] value = { methProbe, cSpearman };
				lowestCorrelationMap.put(gene, value);
			}
		}
		// determine set of probes
		Map<String, String> probeToGene = new HashMap<String, String>();
		for (String gene : genes) {
			String[] value = lowestCorrelationMap.get(gene);
			String probe = value[0];
			if (probeToGene.containsKey(probe)) {
				if (LOG.isInfoEnabled()) {
					LOG.info("createStagingFile(), " + probe + " duplicated in multiple genes");
				}
			}
			probeToGene.put(probe, gene);
		}

		// remove extraneous columns:
		//
		// original methylation__humanmethylation[27|450] file contains
		// multiple columns with the same name, in particular 4 columns (Beta_Value  Gene_Symbol Chromosome  Genomic_Coordinate)
		// for each case.
		// so, count the # of columns in methylation__humanmethylation[27|450] and delete all columns but
		// 1, 2, 6, 10, ...,
		List<String> columnHeaders = dataMatrixMethylationData.getColumnHeaders();
		if ((columnHeaders.size()-1) % 4 != 0) {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), methylation__humanmethylation[27|450] does not have 4 columns per case, aborting...");
			}
			return;
		}
		String previousHeader = "";
		for (int lc = 1; lc < columnHeaders.size(); lc++) {
			String columnHeader = columnHeaders.get(lc);
			if (columnHeader.equals(previousHeader)) {
				dataMatrixMethylationData.ignoreColumn(lc, true);
			}
			else {
				previousHeader = columnHeader;
			}
		}

		// remove 2nd row (row data starts at 0 index, ignoring column headings)
		// of data (containing "Composite Element REF Beta_Value...";
		dataMatrixMethylationData.ignoreRow(0, true);

		// select rows in with lowest correlated probes
		// if probe has the gene lowest correlation file (i.e., is in the $probes array ) then select its methylation row
		List<String> hugoSymbols = new ArrayList<String>();
		List<String> hybridizationRefs = dataMatrixMethylationData.getColumnData(METHYLATION_HYBRIDIZATION_REF_COLUMN_HEADER_NAME).get(0);
		for (int lc = 0; lc < hybridizationRefs.size(); lc++) {
			if (probeToGene.containsKey(hybridizationRefs.get(lc))) {
				// add gene to Hugo_Symbol at row lc
				hugoSymbols.add(probeToGene.get(hybridizationRefs.get(lc)));
			}
			else {
				// ignore probe/row
				hugoSymbols.add("NA");
				dataMatrixCorrelationData.ignoreRow(lc, true);
			}
		}

		// add gene & entrez gene id columns
		dataMatrixMethylationData.addColumn(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, hugoSymbols);
		dataMatrixMethylationData.addColumn(Converter.GENE_ID_COLUMN_HEADER_NAME, new ArrayList<String>());
		dataMatrixMethylationData.setGeneIDColumnHeading(Converter.GENE_ID_COLUMN_HEADER_NAME);

		// perform gene mapping, remove records as needed
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), calling MapperUtil.mapGeneSymbolToID()...");
		}
		MapperUtil.mapGeneSymbolToID(dataMatrixMethylationData, idMapper,
									 Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);

		// convert case ids
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), filtering & converting case ids");
		}
		String[] columnsToIgnore = { Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, Converter.GENE_ID_COLUMN_HEADER_NAME }; // drop Hybridization REF
		dataMatrixMethylationData.convertCaseIDs(Arrays.asList(columnsToIgnore));

		// ensure the first two columns are symbol, id respectively
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), sorting column headers");
		}
		columnHeaders = dataMatrixMethylationData.getColumnHeaders();
		columnHeaders.remove(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.add(0, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.remove(Converter.GENE_ID_COLUMN_HEADER_NAME);
		columnHeaders.add(1, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrixMethylationData.setColumnOrder(columnHeaders);

		// ignore rows with hugo symbol of NA
		List<String> rows = dataMatrixMethylationData.getColumnData(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME).get(0);
		for (int lc = 0; lc < rows.size(); lc++) {
			if (rows.get(lc).equals("NA")) {
				dataMatrixMethylationData.ignoreRow(lc, true);
			}
		}
		
		if (LOG.isInfoEnabled()) {
			//dataMatrixMethylationData.setGeneIDColumnHeading();
			//LOG.info("createStagingFile(), of ", genes.size(), " with measured methylation, only ", lowestCorrelation.size(), " have correlations.\n";);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeStagingFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrixMethylationData);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	
		if (datatypeMetadata.requiresMetafile()){
			if (LOG.isInfoEnabled()) {
				LOG.info("createStagingFile(), writing metadata file.");
			}
			fileUtils.writeMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrixMethylationData);
		}
	}
}


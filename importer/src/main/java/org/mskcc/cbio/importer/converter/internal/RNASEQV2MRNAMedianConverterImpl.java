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
		if (dataMatrices.length != 1) {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), dataMatrices.length != 1, aborting...");
			}
			return;
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
                
                String geneColumnName = HYBRIDIZATION_REF_COLUMN_HEADER_NAME;
                List<String> pairs;
                List<LinkedList<String>> columnData = dataMatrix.getColumnData(HYBRIDIZATION_REF_COLUMN_HEADER_NAME);
                if (!columnData.isEmpty()) {
                    pairs = columnData.get(0);
                } else {
                    pairs = dataMatrix.getColumnData(0); // non standard gene column name
                    geneColumnName = dataMatrix.getColumnHeaders().get(0);
                }
                
		for (int lc = 0; lc < pairs.size(); lc++) {
			String[] parts = pairs.get(lc).trim().split("\\|");
			if (parts.length == 2) {
				String toPart = parts[1];
                                if (!toPart.matches("[0-9]+")) {
                                    if (toPart.matches("[0-9]+_calculated")) {
                                        toPart = toPart.substring(0,toPart.indexOf("_"));
                                    } else {
                                        toPart = parts[0];
                                    }
                                }
				if (LOG.isInfoEnabled()) {
					LOG.info("setting element: " + Arrays.asList(parts) + ", to: " + toPart);
				}
				pairs.set(lc, toPart);
			}
		}

		// add gene symbol column, rename gene id col
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), adding & renaming columns");
		}
		dataMatrix.addColumn(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, new ArrayList<String>());
		dataMatrix.renameColumn(geneColumnName, Converter.GENE_ID_COLUMN_HEADER_NAME);
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
		fileUtils.writeStagingFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrix);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}

		if (datatypeMetadata.requiresMetafile()){
			if (LOG.isInfoEnabled()) {
				LOG.info("createStagingFile(), writing metadata file.");
			}
			fileUtils.writeMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrix);
		}	
	}
}

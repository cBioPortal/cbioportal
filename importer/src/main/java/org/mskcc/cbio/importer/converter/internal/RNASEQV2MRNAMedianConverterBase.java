/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
public abstract class RNASEQV2MRNAMedianConverterBase extends ConverterBaseImpl implements Converter {

	protected static final String HYBRIDIZATION_REF_COLUMN_HEADER_NAME = "Hybridization REF";

	protected Log LOG;
	protected Config config;
	protected FileUtils fileUtils;
	protected CaseIDs caseIDs;
	protected IDMapper idMapper;
	protected ConversionType conversionType;

    @Override
	public void convertData(String portal, String runDate, Boolean applyOverrides) throws Exception
	{
		throw new UnsupportedOperationException();
	}

    @Override
	public void generateCaseLists(String portal) throws Exception
	{
		throw new UnsupportedOperationException();
    }

    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes, boolean applyCaseLists) throws Exception
	{
		throw new UnsupportedOperationException();
    }

	@Override
	public void createStagingFile(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata,
								  DatatypeMetadata datatypeMetadata, DataMatrix[] dataMatrices) throws Exception
	{
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
		logMessage(LOG, "createStagingFile(), cleaning up Hybridization REF column...");
        cleanHybridRefColumn(dataMatrix);        
        
		// add gene symbol column, rename gene id col
		logMessage(LOG, "createStagingFile(), adding & renaming columns");
		addMissingColumns(dataMatrix);

		// perform gene mapping, remove records as needed
		logMessage(LOG, "createStagingFile(), calling MapperUtil.mapDataToGeneID()...");
		MapperUtil.mapGeneIDToSymbol(dataMatrix, idMapper,
									 Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);

		// convert case ids
		logMessage(LOG, "createStagingFile(), filtering & converting case ids");
		String[] columnsToIgnore = { Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME };
		dataMatrix.convertCaseIDs(Arrays.asList(columnsToIgnore));

		// ensure the first two columns are symbol, id respectively
		logMessage(LOG, "createStagingFile(), sorting column headers");
		reorderColumns(dataMatrix);

		// filter out tumor or sample data as required
		logMessage(LOG, "createStagingFile(), filtering columns by sample type.");
		int columnsIgnored = filterColumnsBySampleType(caseIDs, dataMatrix, conversionType);

		if (columnsIgnored == dataMatrix.getColumnHeaders().size()-2) {
			logMessage(LOG, "createStagingFile(), all columns ignored, skipping staging file creation.");
		} 
		else {
			// we need to write out the file
			logMessage(LOG, "createStagingFile(), writing staging file.");
			fileUtils.writeStagingFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrix);

			if (datatypeMetadata.requiresMetafile()){
				logMessage(LOG, "createStagingFile(), writing metadata file.");
				fileUtils.writeMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrix);
			}	
		}

		logMessage(LOG, "createStagingFile(), complete.");
	}

	private void cleanHybridRefColumn(DataMatrix dataMatrix)
	{
		String geneColumnName = HYBRIDIZATION_REF_COLUMN_HEADER_NAME;
        List<String> pairs;
        List<LinkedList<String>> columnData = dataMatrix.getColumnData(HYBRIDIZATION_REF_COLUMN_HEADER_NAME);
        if (!columnData.isEmpty()) {
            pairs = columnData.get(0);
        }
        else {
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
                    }
                    else {
                        toPart = parts[0];
                    }
                }
				logMessage(LOG, "setting element: " + Arrays.asList(parts) + ", to: " + toPart);
				pairs.set(lc, toPart);
			}
		}
	}

	private void addMissingColumns(DataMatrix dataMatrix)
	{
		String geneColumnName = HYBRIDIZATION_REF_COLUMN_HEADER_NAME;
		dataMatrix.addColumn(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, new ArrayList<String>());
		dataMatrix.renameColumn(geneColumnName, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setGeneIDColumnHeading(Converter.GENE_ID_COLUMN_HEADER_NAME);
	}

	private void reorderColumns(DataMatrix dataMatrix) throws Exception
	{
		List<String> headers = dataMatrix.getColumnHeaders();
		headers.remove(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		headers.add(0, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		headers.remove(Converter.GENE_ID_COLUMN_HEADER_NAME);
		headers.add(1, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setColumnOrder(headers);
	}

	protected void removeUsedV1ColumnHeaders(DataMatrix dataMatrix)
	{
		String previousHeader = "";
		List<String> columnHeaders = dataMatrix.getColumnHeaders();
		for (int lc = columnHeaders.size()-1; lc >= 0; lc--) {
			String columnHeader = columnHeaders.get(lc);
			if (columnHeader.equals(previousHeader)) {
				dataMatrix.ignoreColumn(lc, true);
			}
			else {
				previousHeader = columnHeader;
			}
		}
	}
}

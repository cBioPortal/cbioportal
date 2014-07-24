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
 * Class which implements the Converter interface.
 */
public abstract class MRNAMedianConverterBase extends ConverterBaseImpl implements Converter {

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
								  DatatypeMetadata datatypeMetadata, DataMatrix[] dataMatrices) throws Exception {

		// sanity check
		if (dataMatrices.length != 1) {
			if (LOG.isErrorEnabled()) {
				LOG.error("createStagingFile(), dataMatrices.length != 1, aborting...");
			}
			return;
		}
		DataMatrix dataMatrix = dataMatrices[0];

		// add gene id column, rename gene symbol col
		logMessage(LOG, "createStagingFile(), adding & renaming columns");
		addMissingColumns(dataMatrix);

		// perform gene mapping, remove records as needed
		logMessage(LOG, "createStagingFile(), calling MapperUtil.mapGeneSymbolToID()...");
		MapperUtil.mapGeneSymbolToID(dataMatrix, idMapper,
									 Converter.GENE_ID_COLUMN_HEADER_NAME, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);

		// convert case ids
		logMessage(LOG, "createStagingFile(), filtering & converting case ids.");
		String[] columnsToIgnore = { Converter.GENE_SYMBOL_COLUMN_HEADER_NAME, Converter.GENE_ID_COLUMN_HEADER_NAME };
		dataMatrix.convertCaseIDs(Arrays.asList(columnsToIgnore));

		// reorder columns
		logMessage(LOG, "createStagingFile(), reording columns.");
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

	private void addMissingColumns(DataMatrix dataMatrix)
	{
		dataMatrix.addColumn(Converter.GENE_ID_COLUMN_HEADER_NAME, new ArrayList<String>());
		dataMatrix.setGeneIDColumnHeading(Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.renameColumn("Hybridization REF", Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
	}

	private void reorderColumns(DataMatrix dataMatrix) throws Exception
	{
		List<String> columnHeaders = dataMatrix.getColumnHeaders();
		// ensure the first two columns are symbol, id respectively
		columnHeaders.remove(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.add(0, Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);
		columnHeaders.remove(Converter.GENE_ID_COLUMN_HEADER_NAME);
		columnHeaders.add(1, Converter.GENE_ID_COLUMN_HEADER_NAME);
		dataMatrix.setColumnOrder(columnHeaders);
	}
}

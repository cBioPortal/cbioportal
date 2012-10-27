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
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.model.ImportDataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Vector;

/**
 * Class which implements the Converter interface.
 */
public final class CNAConverterImpl implements Converter {

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
	public CNAConverterImpl(final Config config, final FileUtils fileUtils,
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
	 * Creates a staging file from the given import data.
	 *
	 * @param dataSourceMetadata DataSourceMetadata
	 * @param datatypeMetadata DatatypeMetadata
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @param importDataMatrix ImportDataMatrix
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(final DataSourceMetadata dataSourceMetadata, final DatatypeMetadata datatypeMetadata,
								  final PortalMetadata portalMetadata, final ImportData importData) throws Exception {

		ImportDataMatrix importDataMatrix = importData.getImportDataMatrix();

		// perform gene mapping, remove records as needed
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), calling MapperUtil.mapDataToGeneID()...this may take a while...");
		}
		MapperUtil.mapDataToGeneID(importDataMatrix, idMapper,
								   "Gene Symbol", "Locus ID");

		// rename columns
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), renaming columns");
		}
		importDataMatrix.renameColumn("Gene Symbol", "Hugo_Symbol");
		importDataMatrix.renameColumn("Locus ID", "Entrez_Gene_Id");

		// filter and convert case ids
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), filtering & converting case ids");
		}
		String[] columnsToIgnore = { "Hugo_Symbol", "Entrez_Gene_Id" }; // drop Cytoband
		importDataMatrix.filterAndConvertCaseIDs(Arrays.asList(columnsToIgnore));

		// ensure the first two columns are symbol, id respectively
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), sorting column headers");
		}
		Vector<String> columnHeaders = importDataMatrix.getColumnHeaders();
		columnHeaders.setElementAt("Hugo_Symbol", 0);
		columnHeaders.setElementAt("Entrez_Gene_Id", 1);
		importDataMatrix.setColumnOrder(columnHeaders);

		// we need to write out the file
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeStagingFile(dataSourceMetadata, datatypeMetadata, portalMetadata, importData);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}
}

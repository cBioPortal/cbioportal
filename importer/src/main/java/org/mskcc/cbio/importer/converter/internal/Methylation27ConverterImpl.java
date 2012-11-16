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
import org.mskcc.cbio.importer.model.ImportDataMatrix;

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
public final class Methylation27ConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(Methylation27ConverterImpl.class);

	// statics for column identifiers in correlate - methylation file
	private static final String CORRELATE_METH_PROBE_COLUMN_HEADER_NAME = "Meth_Probe";

	// statics for column identifires in <CANCER>.methylation__humanmethylation27 file
	private static final String HYBRIDIZATION_REF_COLUMN_HEADER_NAME = "Hybridization REF";

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
	public Methylation27ConverterImpl(final Config config, final FileUtils fileUtils,
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
	 * @param importDataMatrices ImportDataMatrix[]
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								  final DatatypeMetadata datatypeMetadata, final ImportDataMatrix[] importDataMatrices) throws Exception {

		// sanity check
		if (importDataMatrices.length != 2) {
			throw new IllegalArgumentException("ImportDataMatrices.length != 2, aborting...");
		}

		// determine which matrix is methylation data
		// and which matrix is the correlation data
		ImportDataMatrix importDataMatrixMethylationData = null;
		ImportDataMatrix importDataMatrixCorrelationData = null;
		if (importDataMatrices[0].getColumnHeaders().firstElement().equalsIgnoreCase(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME) &&
			importDataMatrices[1].getColumnHeaders().firstElement().equalsIgnoreCase(HYBRIDIZATION_REF_COLUMN_HEADER_NAME)) {
			importDataMatrixCorrelationData = importDataMatrices[0];
			importDataMatrixMethylationData = importDataMatrices[1];
		}
		else if (importDataMatrices[0].getColumnHeaders().firstElement().equalsIgnoreCase(HYBRIDIZATION_REF_COLUMN_HEADER_NAME) &&
				 importDataMatrices[1].getColumnHeaders().firstElement().equalsIgnoreCase(CORRELATE_METH_PROBE_COLUMN_HEADER_NAME)) {
			importDataMatrixMethylationData = importDataMatrices[0];
			importDataMatrixCorrelationData = importDataMatrices[1];
		}
		else {
			throw new IllegalArgumentException("Cannot determine correlation & methylation data matrices, aborting...");
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		//fileUtils.writeStagingFile(portalMetadata, cancerStudy, datatypeMetadata, importDataMatrixTableConf);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}
}


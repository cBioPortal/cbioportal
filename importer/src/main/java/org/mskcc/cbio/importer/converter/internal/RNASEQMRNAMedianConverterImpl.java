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
 * Class which implements the Converter interface for processing rna-seq (v1) - RPKM files.
 */
public class RNASEQMRNAMedianConverterImpl extends RNASEQV2MRNAMedianConverterImpl implements Converter {

	// our logger
	private static Log LOG = LogFactory.getLog(RNASEQMRNAMedianConverterImpl.class);

    /**
	 * Constructor.
	 *
	 * @param config Config
	 * @param fileUtils FileUtils
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public RNASEQMRNAMedianConverterImpl(Config config, FileUtils fileUtils,
										 CaseIDs caseIDs, IDMapper idMapper) {
		super(config, fileUtils, caseIDs, idMapper);
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

		// rnaseq v1 files have 3 columns per sample (first column is Hybridization REF).
		// discard first & second columns and take third - RPKM
		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), removing  and keepng RPKM column per sample");
		}
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
		
		// everything from here is the same for rna seq v2, lets pass processing to it
		dataMatrices = new DataMatrix[] { dataMatrix };
		super.createStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrices);
	}
}

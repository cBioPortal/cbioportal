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
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class which implements the Converter interface.
 */
public final class ZScoresConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(ZScoresConverterImpl.class);

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
	public ZScoresConverterImpl(final Config config, final FileUtils fileUtils,
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

		// this code assumes dependencies have already been created
		String[] dependencies = datatypeMetadata.getDependencies();
		// sanity check
		if (dependencies.length != 2) {
			throw new IllegalArgumentException("createStagingFile(), dependencies.length != 2, aborting...");
		}

		// we assume dependency staging files have already been created, get paths to dependencies
		DatatypeMetadata[] dependenciesMetadata = getDependencies(dependencies);
		// sanity check
		if (dependenciesMetadata.length != 2) {
			throw new IllegalArgumentException("createStagingFile(), dependenciesMetadata.length != 2, aborting...");
		}

		// verify order is copy number followed by gistic
		if ((dependenciesMetadata[0].getDatatype().contains("expression") || dependenciesMetadata[0].getDatatype().contains("EXPRESSION")) &&
			(dependenciesMetadata[1].getDatatype().contains("cna") || dependenciesMetadata[1].getDatatype().contains("CNA"))) {
			DatatypeMetadata tmp = dependenciesMetadata[0];
			dependenciesMetadata[0] = dependenciesMetadata[1];
			dependenciesMetadata[1] = tmp;
		}
		// sanity check
		if (!(dependenciesMetadata[0].getDatatype().contains("cna") || dependenciesMetadata[0].getDatatype().contains("CNA")) ||
			!(dependenciesMetadata[1].getDatatype().contains("expression") || dependenciesMetadata[1].getDatatype().contains("EXPRESSION"))) {
			throw new IllegalArgumentException("createStagingFile(), cannot determine cna and expression datatype order, aborting...");
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}
		fileUtils.writeZScoresStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dependenciesMetadata);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}

	/**
	 * Helper function to determine DatatypeMetadata dependencies.
	 *
	 * @param dependencies DatatypeMetadata[]
	 * @return String[]
	 */
	private DatatypeMetadata[] getDependencies(final String[] dependencies) {

		// this is what we return
		DatatypeMetadata[] toReturn = new DatatypeMetadata[dependencies.length];

		for (int lc = 0; lc < dependencies.length; lc++) {
			String dependency = dependencies[lc];
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata()) {
				if (dependency.equals(datatypeMetadata.getDatatype())) {
					toReturn[lc] = datatypeMetadata;
				}
			}
		}


		// outta here
		return toReturn;
	}
}

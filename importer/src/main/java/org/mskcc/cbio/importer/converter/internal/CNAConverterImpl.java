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
import org.mskcc.cbio.importer.Converter;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JTable;

/**
 * Class which implements the Converter interface.
 */
final class CNAConverterImpl implements Converter {

	// our logger
	private static final Log LOG = LogFactory.getLog(ConverterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	/**
	 * Constructor.
     *
     * Takes a Config reference.
	 * Takes a FileUtils reference.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 */
	public CNAConverterImpl(final Config config, final FileUtils fileUtils) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
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
	 * Returns the given file contents in a JTable.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @param JTable
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(final PortalMetadata portalMetadata,
								  final ImportData importData, final JTable jtable) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile()");
		}
	}
}

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
package org.mskcc.cbio.importer.mapper.internal;

// imports
import org.mskcc.cbio.importer.IDMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bridgedb.BridgeDb;
import org.bridgedb.bio.BioDataSource;

/**
 * Class which provides bridgedb services.
 */
public final class BridgeDBIDMapper implements IDMapper {

	// our logger
	private static final Log LOG = LogFactory.getLog(BridgeDBIDMapper.class);

	// ref to bridge db mapper
	private org.bridgedb.IDMapper mapper;

	/**
	 * Default Constructor.
	 */
	public BridgeDBIDMapper() {}

	/**
	 * Used to initialize the mapper.
	 *
	 * @param connectionString String
	 * @throws Exception
	 */
	@Override
	public void initMapper(final String connectionString) throws Exception {

		// we need to prepend bridgedb protocol to connection string
		String bridgeDBConnectionString = "idmapper-" + connectionString;

		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), bridgeDBConnectionString: " + bridgeDBConnectionString);
		}

		Class.forName("org.bridgedb.rdb.IDMapperRdb");
        mapper = BridgeDb.connect(bridgeDBConnectionString);
        BioDataSource.init();
	}

	/**
	 * For the given symbol, return id.
	 *
	 * @param geneSymbol String
	 * @return String
	 */
	@Override
	public String entrezSymbolToNumber(final String geneSymbol) {

		return "";
	}
}

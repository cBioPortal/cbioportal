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

import org.bridgedb.Xref;
import org.bridgedb.BridgeDb;
import org.bridgedb.XrefIterator;
import org.bridgedb.AttributeMapper;
import org.bridgedb.bio.BioDataSource;

import java.util.Set;
import java.util.HashMap;


/**
 * Class which provides bridgedb services.
 */
public class BridgeDBIDMapper implements IDMapper {

	// our logger
	private static Log LOG = LogFactory.getLog(BridgeDBIDMapper.class);

	// connection string
	private String connectionString;

	// our map of gene symbols to entrez ids
	private HashMap<String, String> symbolToIDMap;
	private HashMap<String, String> idToSymbolMap;

	/**
	 * Default Constructor.
	 */
	public BridgeDBIDMapper() {
		connectionString = "";
	}

	/**
	 * Used to initialize the mapper.
	 *
	 * @param connectionString String
	 * @throws Exception
	 */
	@Override
	public void initMapper(String connectionString) throws Exception {

		// don't init if we already have with this connection string
		if (this.connectionString.equals(connectionString)) return;
		this.connectionString = connectionString;

		// we need to prepend bridgedb protocol to connection string
		String bridgeDBConnectionString = "idmapper-" + connectionString;

		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), bridgeDBConnectionString: " + bridgeDBConnectionString);
		}

		Class.forName("org.bridgedb.rdb.IDMapperRdb");
        AttributeMapper mapper = (AttributeMapper)BridgeDb.connect(bridgeDBConnectionString);
        BioDataSource.init();

		// populate our maps
		symbolToIDMap = new HashMap<String, String>();
		idToSymbolMap = new HashMap<String, String>();
		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), building maps");
		}
		if (mapper instanceof XrefIterator) {
			for (Xref xref : ((XrefIterator)mapper).getIterator(BioDataSource.ENTREZ_GENE)) {
				for (String symbol : mapper.getAttributes(xref, "Symbol")) {
					symbolToIDMap.put(symbol, xref.getId());
					idToSymbolMap.put(xref.getId(), symbol);
				}
			}
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), building maps complete");
		}
	}

	/**
	 * For the given symbol, return id.
	 *
	 * @param geneSymbol String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String symbolToEntrezID(String geneSymbol) throws Exception {
		return (symbolToIDMap.containsKey(geneSymbol)) ? symbolToIDMap.get(geneSymbol) : "";
	}

	/**
	 * For the entrezID, return symbol.
	 *
	 * @param entrezID String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String entrezIDToSymbol(String entrezID) throws Exception {
		return (idToSymbolMap.containsKey(entrezID)) ? idToSymbolMap.get(entrezID) : "";
	}
}

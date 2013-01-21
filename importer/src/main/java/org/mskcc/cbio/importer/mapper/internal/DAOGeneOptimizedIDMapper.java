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
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Class which provides bridgedb services.
 */
public class DAOGeneOptimizedIDMapper implements IDMapper {

	// our logger
	private static final Log LOG = LogFactory.getLog(DAOGeneOptimizedIDMapper.class);

	// identifier for start of miRNA
	private static final String MIRNA_PREFIX = "hsa-";

	// ref to DAOGeneOptimized
	DaoGeneOptimized daoGeneOptimized;

	/**
	 * For the given symbol, return id.
	 *
	 * @param geneSymbol String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String symbolToEntrezID(String geneSymbol) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("symbolToEntrez(): " + geneSymbol);
		}
		if (geneSymbol.startsWith(MIRNA_PREFIX) ||
			geneSymbol.startsWith(MIRNA_PREFIX.toUpperCase())) {
			return geneSymbol;
		}
		CanonicalGene gene = guessGene(geneSymbol);
		return (gene != null) ? Long.toString(gene.getEntrezGeneId()) : "";
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
		if (LOG.isDebugEnabled()) {
			LOG.debug("entrezIDToSymbol(): " + entrezID);
		}
		CanonicalGene gene = guessGene(entrezID);
		return (gene != null) ? gene.getHugoGeneSymbolAllCaps() : "";
	}

	/**
	 * Helper function to process DaoGeneOptimized list.
	 *
	 * @param IDOrSymbol
	 * @return CanonicalGene
	 * @throws Exception
	 */
	private CanonicalGene guessGene(String IDOrSymbol) throws Exception {

		if (daoGeneOptimized == null) {
			daoGeneOptimized = DaoGeneOptimized.getInstance();
		}
		
		List<CanonicalGene> geneList = daoGeneOptimized.guessGene(IDOrSymbol);
		if (geneList != null && LOG.isDebugEnabled()) {
			LOG.debug("guessGene(), returned list size: " + geneList.size());
		}
		return (geneList != null && geneList.size() > 0) ? geneList.get(0) : null;
	}
}

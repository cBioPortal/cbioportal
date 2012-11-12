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
package org.mskcc.cbio.importer.util;

// imports
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.model.ImportDataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;

/**
 * Class which provides mapping utility services.
 */
public final class MapperUtil {

	// our logger
	private static final Log LOG = LogFactory.getLog(MapperUtil.class);

	/**
	 * Given a gene symbol column and gene ID column within an ImportDataMatrix,
	 * obtain gene IDs for all entries in the column.  Drop rows for which a gene
	 * ID cannot be found.
	 *
	 * @param importDataMatrix ImportDataMatrix
	 * @param idMapper IDMapper
	 * @param geneSymbolColumnName String
	 * @param geneIDColumnName String
	 * @throws Exception
	 */
	public static void mapDataToGeneID(final ImportDataMatrix importDataMatrix, final IDMapper idMapper,
									   final String geneSymbolColumnName, final String geneIDColumnName) throws Exception {

		// get refs to geneSymbols and geneIDs columns
		Vector<String> geneSymbols = importDataMatrix.getColumnData(geneSymbolColumnName);
		Vector<String> geneIDs = importDataMatrix.getColumnData(geneIDColumnName);

		// sanity check
		if (geneSymbols.size() != geneIDs.size()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("mapDataToGeneID(), geneSymbols column size != geneIDs column size, aborting.");
			}
			return;
		}

		// do the mapping, ignore rows that are missing id's
		for (int lc = 0; lc < geneSymbols.size(); lc++) {
			String geneSymbol = geneSymbols.elementAt(lc);
			if (geneSymbol == "") {
				if (LOG.isDebugEnabled()) {
					LOG.debug("mapDataToGeneID(), geneSymbol is empty, ignoring row: " + lc);
				}
				importDataMatrix.ignoreRow(lc);
				continue;
			}
			String entrezID = idMapper.symbolToEntrezID(geneSymbol);
			if (entrezID == "") {
				if (LOG.isDebugEnabled()) {
					LOG.debug("mapDataToGeneID(), cannot find entrez id for geneSymbol: " + geneSymbol + ", ignoring row: " + lc);
				}
				importDataMatrix.ignoreRow(lc);
				continue;
			}
			geneIDs.setElementAt(entrezID, lc);
		}
	}
}

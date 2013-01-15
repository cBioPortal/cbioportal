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
import org.mskcc.cbio.importer.model.DataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Class which provides mapping utility services.
 */
public class MapperUtil {

	// our logger
	private static final Log LOG = LogFactory.getLog(MapperUtil.class);
	private static final String UNKNOWN_ID_SYMBOL = "UNKNOWN"; 

	// type of mapping enum
	private enum MappingDirection {
		ID_TO_SYMBOL, SYMBOL_TO_ID
	}

	/**
	 * Given a gene ID column and gene symbol column within an DataMatrix,
	 * obtain gene symbols for all entries in the column.  Drop rows for which a gene
	 * symbol cannot be found.
	 *
	 * @param dataMatrix DataMatrix
	 * @param idMapper IDMapper
	 * @param geneIDColumnName String
	 * @param geneSymbolColumnName String
	 * @throws Exception
	 */
	public static void mapGeneIDToSymbol(DataMatrix dataMatrix, IDMapper idMapper,
										 String geneIDColumnName, String geneSymbolColumnName) throws Exception {

		doMapping(dataMatrix, idMapper, geneIDColumnName, geneSymbolColumnName, MappingDirection.ID_TO_SYMBOL);
	}

	/**
	 * Given a gene symbol column and gene ID column within an DataMatrix,
	 * obtain gene IDs for all entries in the column.  Drop rows for which a gene
	 * ID cannot be found.
	 *
	 * @param dataMatrix DataMatrix
	 * @param idMapper IDMapper
	 * @param geneIDColumnName String
	 * @param geneSymbolColumnName String
	 * @throws Exception
	 */
	public static void mapGeneSymbolToID(DataMatrix dataMatrix, IDMapper idMapper,
										 String geneIDColumnName, String geneSymbolColumnName) throws Exception {
		doMapping(dataMatrix, idMapper, geneSymbolColumnName, geneIDColumnName, MappingDirection.SYMBOL_TO_ID);
	}

	/**
	 * Helper function for public interface.
	 *
	 * @param dataMatrix DataMatrix
	 * @param idMapper IDMapper
	 th	 * @param srcColumnName String
	 * @param targetColumnName String
	 * @param mappingDirection MappingDirectory
	 * @throws Exception
	 */
	private static void doMapping(DataMatrix dataMatrix, IDMapper idMapper,
								  String srcColumnName, String targetColumnName,
								  MappingDirection mappingDirection) throws Exception {

		// get refs to src and target columns
		List<String> srcColumnData = dataMatrix.getColumnData(srcColumnName).get(0);
		List<String> targetColumnData = dataMatrix.getColumnData(targetColumnName).get(0);

		// do the mapping, ignore rows that are missing id's
		for (int lc = 0; lc < srcColumnData.size(); lc++) {
			String src = srcColumnData.get(lc);
			if (LOG.isDebugEnabled()) {
				LOG.debug("doMapping(), src: " + src);
			}
			if (src == "") {
				if (LOG.isDebugEnabled()) {
					LOG.debug("doMapping(), src is empty, ignoring row: " + lc);
				}
				dataMatrix.ignoreRow(lc, true);
				try {
					targetColumnData.set(lc, UNKNOWN_ID_SYMBOL);
				}
				catch(IndexOutOfBoundsException e) {
					targetColumnData.add(UNKNOWN_ID_SYMBOL);
				}
				continue;
			}
			String target = (mappingDirection == MappingDirection.SYMBOL_TO_ID) ?
				idMapper.symbolToEntrezID(src) : idMapper.entrezIDToSymbol(src);
			if (LOG.isDebugEnabled()) {
				LOG.debug("doMapping(), target: " + target);
			}
			if (target == "") {
				if (LOG.isDebugEnabled()) {
					LOG.debug("doMapping(), cannot find target for src: " + src + ", ignoring row: " + lc);
				}
				dataMatrix.ignoreRow(lc, true);
				try {
					targetColumnData.set(lc, UNKNOWN_ID_SYMBOL);
				}
				catch(IndexOutOfBoundsException e) {
					targetColumnData.add(UNKNOWN_ID_SYMBOL);
				}
				continue;
			}
			try {
				targetColumnData.set(lc, target);
			}
			catch(IndexOutOfBoundsException e) {
				targetColumnData.add(target);
			}
		}
	}
}

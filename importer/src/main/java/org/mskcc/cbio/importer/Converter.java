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
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

/**
 * Interface used to convert portal data.
 */
public interface Converter {

	public static final String GENE_ID_COLUMN_HEADER_NAME = "Entrez_Gene_Id";
	public static final String GENE_SYMBOL_COLUMN_HEADER_NAME = "Hugo_Symbol";
	public static final String CASE_DELIMITER = "\t";

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
	void convertData(final String portal) throws Exception;

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
	void generateCaseLists(final String portal) throws Exception;

	/**
	 * Applies overrides to the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
	void applyOverrides(final String portal) throws Exception;

	/**
	 * Creates a staging file from the given import data.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy CancerStudyMetadata
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrices DataMatrix[]
	 * @throws Exception
	 */
	void createStagingFile(final PortalMetadata portalMetadata, final CancerStudyMetadata cancerStudyMetadata,
						   final DatatypeMetadata datatypeMetadata, final DataMatrix[] dataMatrices) throws Exception;
}

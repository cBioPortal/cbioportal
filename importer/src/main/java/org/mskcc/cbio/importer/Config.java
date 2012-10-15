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
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import java.util.Collection;

/**
 * Interface used to get/set configuration properties.
 */
public interface Config {

	/**
	 * Gets a collection of TumorTypeMetadata.
	 *
	 * @return Collection<TumorTypeMetadata>
	 */
	Collection<TumorTypeMetadata> getTumorTypeMetadata();

	/**
	 * Gets a collection of DatatypeMetadata.
	 *
	 * @return Collection<DatatypeMetadata>
	 */
	Collection<DatatypeMetadata> getDatatypeMetadata();

	/**
	 * Gets a PortalMetadata object given a portal name.
	 *
     * @param portal String
	 * @return PortalMetadata
	 */
	PortalMetadata getPortalMetadata(String portal);

	/**
	 * Gets ReferenceMetadata for the given referenceType.
	 *
	 * @param referenceType String
	 * @return ReferenceMetadata
	 */
	ReferenceMetadata getReferenceMetadata(String referenceType);

	/**
	 * Gets DataSourceMetadata for the given datasource.
	 *
	 * @param dataSource String
	 * @return DataSourceMetadata
	 */
	DataSourceMetadata getDataSourceMetadata(String dataSource);

	/**
	 * Sets DataSourceMetadata (currently only stores latest run downloaded).
	 *
     * @param dataSourceMetadata DataSourceMetadata
	 */
	void setDataSourceMetadata(final DataSourceMetadata dataSourceMetadata);
}

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
import org.mskcc.cbio.importer.model.FirehoseDatatypeMetadata;
import org.mskcc.cbio.importer.model.FirehoseDownloadMetadata;

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
	 * Gets FirehoseDownloadMetadata.
	 *
	 * @return FirehoseDownloadMetadata
	 */
	FirehoseDownloadMetadata getFirehoseDownloadMetadata();

	/**
	 * Gets a collection of FirehoseDatatypeMetadata.
	 *
	 * @return Collection<FirehoseDatatypeMetadata>
	 */
	Collection<FirehoseDatatypeMetadata> getFirehoseDatatypeMetadata();

	/**
	 * Sets FirehoseDownloadMetadata.  Really only used to store
     * stddata/analysis run dates.
	 *
     * @param firehoseDownloadMetadata FirehoseDownloadMetadata
	 * @return FirehoseDownloadMetadata
	 */
	void setFirehoseDownloadMetadata(final FirehoseDownloadMetadata firehoseDownloadMetadata);
}

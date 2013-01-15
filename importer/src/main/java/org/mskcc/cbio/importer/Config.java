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
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.CaseIDFilterMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.model.ClinicalAttributesMetadata;

import java.util.Collection;

/**
 * Interface used to get/set configuration properties.
 */
public interface Config {

	// const used when requesting all of something
	public static final String ALL = "all";

	/**
	 * Gets a TumorTypeMetadata object via tumorType.
	 * If tumorType == Config.ALL, all are returned.
	 *
	 * @param tumortype String
	 * @return Collection<TumorTypeMetadata>
	 */
	Collection<TumorTypeMetadata> getTumorTypeMetadata(String tumorType);

	/**
	 * Function to get tumor types to download as String[]
	 *
	 * @return String[]
	 */
	String[] getTumorTypesToDownload();

	/**
	 * Gets a DatatypeMetadata object for the given datatype name.
	 * If datatype == Config.ALL, all are returned.
	 *
	 * @param datatype String
	 * @return Collection<DatatypeMetadata>
	 */
	Collection<DatatypeMetadata> getDatatypeMetadata(String datatype);

	/**
	 * Gets a collection of Datatype names for the given portal/cancer study.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @return Collection<String>
	 */
	Collection<DatatypeMetadata> getDatatypeMetadata(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata);

	/**
	 * Function to get datatypes to download as String[].
	 *
	 * @param dataSourcesMetadata DataSourcesMetadata
	 * @return String[]
	 * @throws Exception
	 */
	String[] getDatatypesToDownload(DataSourcesMetadata dataSourcesMetadata) throws Exception;

	/**
	 * Function to determine the datatype(s)
	 * of the datasource file (the file that was fetched from a datasource).
	 *
	 * @param dataSourcesMetadata DataSourcesMetadata
	 * @param filename String
	 * @return Collection<DatatypeMetadata>
	 * @throws Exception
	 */
	Collection<DatatypeMetadata> getFileDatatype(DataSourcesMetadata dataSourcesMetadata, String filename)  throws Exception;

	/**
	 * Gets a collection of CaseIDFilterMetadata.
	 * If filterName == Config.ALL, all are returned.
	 *
	 * @param filterName String
	 * @return Collection<CaseIDFilterMetadata>
	 */
	Collection<CaseIDFilterMetadata> getCaseIDFilterMetadata(String filterName);

	/**
	 * Gets a collection of CaseListMetadata.
	 * If caseListFilename == Config.ALL, all are returned.
	 *
	 * @param caseListFilename String
	 * @return Collection<CaseListMetadata>
	 */
	Collection<CaseListMetadata> getCaseListMetadata(String caseListFilename);

	/**
	 * Gets a collection of ClinicalAttributesMetadata.
	 * If clinicalAttributeColumnHeader == Config.ALL, all are returned.
	 *
	 * @param clinicalAttributeColumnHeader String
	 * @return Collection<ClinicalAttributesMetadata>
	 */
	Collection<ClinicalAttributesMetadata> getClinicalAttributesMetadata(String clinicalAttributeColumnHeader);

	/**
	 * Updates (or inserts) the given ClinicalAttributesMetadata object.
	 *
	 * @param clinicalAttributesMetadata ClinicalAttributesMetadata
	 */
	void updateClinicalAttributesMetadata(ClinicalAttributesMetadata clinicalAttributesMetadata);

	/**
	 * Gets a PortalMetadata object given a portal name.
	 * If portalName == Config.ALL, all are returned.
	 *
     * @param portalName String
	 * @return Collection<PortalMetadata>
	 */
	Collection<PortalMetadata> getPortalMetadata(String portalName);

	/**
	 * Gets ReferenceMetadata for the given referenceType.
	 * If referenceType == Config.ALL, all are returned.
	 *
	 * @param referenceType String
	 * @return Collection<ReferenceMetadata>
	 */
	Collection<ReferenceMetadata> getReferenceMetadata(String referenceType);

	/**
	 * Gets DataSourcesMetadata for the given dataSource.  If dataSource == Config.ALL,
	 * all are returned.
	 *
	 * @param dataSource String
	 * @return Collection<DataSourcesMetadata>
	 */
	Collection<DataSourcesMetadata> getDataSourcesMetadata(String dataSource);

	/**
	 * Gets all the cancer studies for a given portal.
	 *
     * @param portal String
	 * @return Collection<CancerStudyMetadata>
	 */
	Collection<CancerStudyMetadata> getCancerStudyMetadata(String portalName);
}

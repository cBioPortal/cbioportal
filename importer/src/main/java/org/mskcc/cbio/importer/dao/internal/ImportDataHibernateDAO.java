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
package org.mskcc.cbio.importer.dao.internal;

// imports
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.dao.ImportDataDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Hibernate implementation of ImportDataDAO.
 */
@Repository
class ImportDataHibernateDAO implements ImportDataDAO {

	// our logger
	private static final Log LOG = LogFactory.getLog(ImportDataHibernateDAO.class);

    // session factory prop/methods used by spring
    private SessionFactory sessionFactory;
    public SessionFactory getSessionFactory() { return sessionFactory; }
    public void setSessionFactory(SessionFactory sessionFactory) { this.sessionFactory = sessionFactory; }
        
    // a shortcut to get current session
    private Session getSession() { return getSessionFactory().getCurrentSession(); }

	/**
	 * Persists the given ImportData object.
	 *
	 * @param importData ImportData
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void importData(final ImportData importData) {

		Session session = getSession();

		// check for existing object
		ImportData existing = getImportDataByTumorAndDatatype(importData.getTumorType(), importData.getDatatype());
		if (existing != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), ImportData object for tumor type: " + importData.getTumorType() +
						 " and datatype: " + importData.getDatatype() + " already exists, manually merging.");
			}
			existing.setDataSource(importData.getDataSource());
			existing.setTumorType(importData.getTumorType());
			existing.setDatatype(importData.getDatatype());
			existing.setRunDate(importData.getRunDate());
			existing.setCanonicalPathToData(importData.getCanonicalPathToData());
			existing.setDigest(importData.getDigest());
			session.update(existing);
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), ImportData object for tumor type: " + importData.getTumorType() +
						 " and datatype: " + importData.getDatatype() + " does not exist, saving.");
				session.save(importData);
			}
		}

		session.flush();
		session.clear();
		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), importData object has been successfully saved or merged.");
		}
	}

    /**
     * Functon to retrieve all ImportData.
	 *
	 * @return Collection<ImportData>
     */
    @Transactional(propagation=Propagation.REQUIRED)
    public Collection<ImportData> getImportData() {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataAll");
        List<ImportData> toReturn = query.list();
        return (toReturn.size() > 0) ? new ArrayList<ImportData>(toReturn) : Collections.EMPTY_SET;
    }

    /**
     * Functon to retrieve ImportData via tumor type and data type.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @return ImportData
     */
    @Transactional(propagation=Propagation.REQUIRED)
    public ImportData getImportDataByTumorAndDatatype(final String tumorType, final String datatype) {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataByTumorAndDatatype");
		query.setParameter("tumortype", tumorType);
		query.setParameter("datatype", datatype);
		return (ImportData)query.uniqueResult();
    }
}

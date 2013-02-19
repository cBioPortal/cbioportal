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
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;

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
 * Hibernate implementation of ImportDataRecordDAO.
 */
@Repository
class ImportDataRecordHibernateDAO implements ImportDataRecordDAO {

	// our logger
	private static Log LOG = LogFactory.getLog(ImportDataRecordHibernateDAO.class);

    // session factory prop/methods used by spring
    private SessionFactory sessionFactory;
    public SessionFactory getSessionFactory() { return sessionFactory; }
    public void setSessionFactory(SessionFactory sessionFactory) { this.sessionFactory = sessionFactory; }
        
    // a shortcut to get current session
    private Session getSession() { return getSessionFactory().getCurrentSession(); }

	/**
	 * Persists the given ImportDataRecord object.
	 *
	 * @param importDataRecord ImportDataRecord
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void importDataRecord(ImportDataRecord importDataRecord) {

		Session session = getSession();

		// check for existing object
		ImportDataRecord existing = getImportDataRecordByTumorAndDatatypeAndDataFilenameAndRunDate(importDataRecord.getTumorType(),
																								   importDataRecord.getDatatype(),
																								   importDataRecord.getDataFilename(),
																								   importDataRecord.getRunDate());
		if (existing == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importDataRecord(), ImportDataRecord object for" +
						 " tumor type: " + importDataRecord.getTumorType() +
						 " datatype: " + importDataRecord.getDatatype() +
						 " data filename: " + importDataRecord.getDataFilename() +
						 " run date: " + importDataRecord.getRunDate() +
						 " does not exist, saving.");
				session.save(importDataRecord);
			}
			session.flush();
			session.clear();

			if (LOG.isInfoEnabled()) {
				LOG.info("importDataRecord(), importDataRecord object has been successfully saved.");
			}
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("ImportDataRecord object for" +
					 " tumor type: " + importDataRecord.getTumorType() +
					 " datatype: " + importDataRecord.getDatatype() +
					 " data filename: " + importDataRecord.getDataFilename() +
					 " run date: " + importDataRecord.getRunDate() +
					 " already exists, not saving.");
		}
	}

    /**
     * Functon to retrieve all ImportDataRecord.
	 *
	 * @return Collection<ImportDataRecord>
     */
	@Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Collection<ImportDataRecord> getImportDataRecords() {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataRecordAll");
        List<ImportDataRecord> toReturn = query.list();
        return (toReturn.size() > 0) ? new ArrayList<ImportDataRecord>(toReturn) : Collections.EMPTY_SET;
    }

    /**
     * Functon to retrieve ImportDataRecord via tumor type, data type, and center.
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param center String
	 * @param runDate String
	 * @return ImportDataRecord
     */
	@Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Collection<ImportDataRecord> getImportDataRecordByTumorTypeAndDatatypeAndCenterAndRunDate(String tumorType, String datatype, String center, String runDate) {
		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataRecordByTumorTypeAndDatatypeAndCenterAndRunDate");
		query.setParameter("tumortype", tumorType);
		query.setParameter("datatype", datatype);
		query.setParameter("center", center);
		query.setParameter("rundate", runDate);
        List<ImportDataRecord> toReturn = query.list();
        return (toReturn.size() > 0) ? new ArrayList<ImportDataRecord>(toReturn) : Collections.EMPTY_SET;
    }

    /**
     * Functon to retrieve ImportDataRecord via tumor type and datatype and data filename
	 *
	 * @param tumorType String
	 * @param dataType String
	 * @param dataFilename String
	 * @param runDate String
	 * @return ImportDataRecord
     */
	@Override
    @Transactional(propagation=Propagation.REQUIRED)
	public ImportDataRecord getImportDataRecordByTumorAndDatatypeAndDataFilenameAndRunDate(String tumorType, String datatype, String dataFilename, String runDate) {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataRecordByTumorAndDatatypeAndDataFilenameAndRunDate");
		query.setParameter("tumortype", tumorType);
		query.setParameter("datatype", datatype);
		query.setParameter("datafilename", dataFilename);
		query.setParameter("rundate", runDate);
        return (ImportDataRecord)query.uniqueResult();
    }

	/**
	 * Function to delete records with the given dataSource
	 *
	 * @param dataSource String
	 */
	@Override
    @Transactional(propagation=Propagation.REQUIRED)
	public void deleteByDataSource(String dataSource) {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.deleteByDataSource");
		query.setParameter("datasource", dataSource);
        query.executeUpdate();
	}
}

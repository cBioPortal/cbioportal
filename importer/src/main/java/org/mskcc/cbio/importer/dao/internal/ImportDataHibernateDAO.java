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
		ImportData existing = getImportDataByCancerAndDatatype(importData.getCancerType(), importData.getDatatype());
		if (existing != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), ImportData object for cancer type: " + importData.getCancerType() +
						 " and datatype: " + importData.getDatatype() + " already exists, manually merging.");
			}
			existing.setCancerType(importData.getCancerType());
			existing.setDatatype(importData.getDatatype());
			existing.setRunDate(importData.getRunDate());
			existing.setURLToData(importData.getURLToData());
			existing.setDigest(importData.getDigest());
			session.update(existing);
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), ImportData object for cancer type: " + importData.getCancerType() +
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
     * Functon to retrieve ImportData via cancer type and data type.
	 *
	 * @param cancerType String
	 * @param dataType String
	 * @return ImportData
     */
    @Transactional(propagation=Propagation.REQUIRED)
    public ImportData getImportDataByCancerAndDatatype(final String cancerType, final String datatype) {

		Session session = getSession();
		Query query = session.getNamedQuery("org.mskcc.cbio.import.model.importDataByCancerAndDatatype");
		query.setParameter("cancertype", cancerType);
		query.setParameter("datatype", datatype);
		return (ImportData)query.uniqueResult();
    }
}

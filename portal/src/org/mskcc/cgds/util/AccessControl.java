// package
package org.mskcc.cgds.util;

// imports
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.ProtocolException;

import java.util.List;

/**
 * Utilities for managing access control.
 *
 * @author Benjamin Gross
 */
public interface AccessControl {

    /**
     * Gets Cancer Studies. Used by QueryBuilder.
     *
     * @return List<CancerStudy>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    List<CancerStudy> getCancerStudiesAsList() throws DaoException, ProtocolException;

    /**
     * Gets Cancer Studies. Used by Webservice.
     *
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    String getCancerStudiesAsTable() throws DaoException, ProtocolException;

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return boolean
     * @throws DaoException
     */
    boolean checkAccess(String stableStudyId) throws DaoException;
}

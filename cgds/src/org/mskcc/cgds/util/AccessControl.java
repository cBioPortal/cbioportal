// package
package org.mskcc.cgds.util;

// imports
import org.mskcc.cgds.model.SecretKey;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.ProtocolException;

/**
 * Utilities for managing access control.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public interface AccessControl {

    /**
     * takes a cleartext key, encrypts it and adds the encrypted key to the dbms.
     *
     * @param userKey
     * @return a SecretKey constructed from the encrypted key
     * @throws DaoException
     */
    public SecretKey createSecretKey(String userKey) throws DaoException;

    /**
     * return true if userKey is one of the secret keys; false otherwise.
     * assumes few keys.
     *
     * @param userKey
     * @return
     * @throws DaoException
     */
    public boolean checkKey(String userKey) throws DaoException;

    /**
     * Return true if the user can access the study, false otherwise.
     * Also works properly if no user is specified.
     * <p/>
     * To avoid circumvention of security controls, ALL cancer study access should pass through this function.
     *
     * @param email   the user's email address, or null if no user is specified (no user is logged in).
     * @param stableStudyId
     * @return true if the user can access the study identified by studyId now, false otherwise
     * @throws DaoException
     */
    public boolean checkAccess(String email, String key, String stableStudyId) throws DaoException;

    /**
     * Gets Cancer Studies.
     *
     * @param email     Email Identifier.
     * @param key       Key.
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    public String getCancerStudies(String email, String key) throws DaoException, ProtocolException;

    /**
     * Get user credentials for given email address.
	 *
     * @param email
     * @return Table output.
     * @throws DaoException Database Error.
	 */
    public String getUserCredentials(String email) throws DaoException;
}

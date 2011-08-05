// package
package org.mskcc.cgds.web_api;

// imports
import org.mskcc.cgds.dao.DaoException;

/**
 * Interface for accessing user credentials.
 *
 * We made this an interface rather than following
 * existing web_api class pattern so implementation
 * class can be spring - bean managed.  When managed,
 * we can take advantage of spring-security method security.
 *
 * @author Benjamin Gross
 */
public interface UserCredentials {

    /**
     * Get user auth tokens for given email address.
     *
     * @param email
     * @return Table output.
     * @throws DaoException Database Error.
     */
    public String getUserCredentials(String email) throws DaoException;
}

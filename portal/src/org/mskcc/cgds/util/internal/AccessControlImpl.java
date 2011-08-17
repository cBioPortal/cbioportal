// package
package org.mskcc.cgds.util.internal;

// imports
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.SecretKey;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.UserAuthorities;

import org.mskcc.cgds.web_api.ProtocolException;

import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoSecretKey;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoUserAuthorities;
import org.mskcc.cgds.dao.DaoUserAccessRight;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.List;
import java.util.ArrayList;

/**
 * Utilities for managing access control.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public class AccessControlImpl implements AccessControl {

	// ref to log
	private static Log log = LogFactory.getLog(AccessControlImpl.class);

    /**
     * takes a cleartext key, encrypts it and adds the encrypted key to the dbms.
     *
     * @param userKey
     * @return a SecretKey constructed from the encrypted key
     * @throws DaoException
     */
    public SecretKey createSecretKey(String userKey) throws DaoException {

        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(userKey);
        SecretKey secretKey = new SecretKey();
        secretKey.setEncryptedKey(encryptedPassword);
        DaoSecretKey.addSecretKey(secretKey);
        return secretKey;
    }

    /**
     * return true if userKey is one of the secret keys; false otherwise.
     * assumes few keys.
     *
     * @param userKey
     * @return
     * @throws DaoException
     */
    public boolean checkKey(String userKey) throws DaoException {
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();

		if (log.isDebugEnabled()) {
			log.debug("checkKey(), userKey: " + userKey);
		}

        ArrayList<SecretKey> allKeys = DaoSecretKey.getAllSecretKeys();
        for (SecretKey secretKey : allKeys) {
                if (passwordEncryptor.checkPassword(userKey, secretKey.getEncryptedKey())) {
                    return true;
                }
        }

		if (log.isDebugEnabled()) {
            log.debug("checkKey(), userKey is not one of the secret keys, returning false.");
		}

        return false;
    }

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
    public boolean checkAccess(String email, String key, String stableStudyId) throws DaoException {

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(stableStudyId);
        if (null == cancerStudy) {
            return false;
        }
        if (cancerStudy.isPublicStudy()) {
            return true;
        }

        // is secret key good?
        if (!checkKey(key)) {
            return false;
        }

        // does UserAccessRight contain user, studyId?
        return DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy(email,
                cancerStudy.getInternalId());
    }

    /**
     * Gets Cancer Studies.
     *
     * @param email     Email Identifier.
     * @param key       Key.
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    public String getCancerStudies(String email, String key) throws DaoException, ProtocolException {

        ArrayList<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();
        ArrayList<CancerStudy> accessibleCancerStudies = new ArrayList<CancerStudy>();
        for (CancerStudy cancerStudy : cancerStudyList) {
            if (checkAccess(email, key, cancerStudy.getCancerStudyStableId())) {
                accessibleCancerStudies.add(cancerStudy);
            }
        }

        StringBuffer buf = new StringBuffer();
        if (accessibleCancerStudies.size() > 0) {

            buf.append("cancer_study_id\tname\tdescription\n");
            for (CancerStudy cancerStudy : accessibleCancerStudies) {
                
                // changed to output stable identifier, instead of internal integer identifer.
                buf.append(cancerStudy.getCancerStudyStableId() + "\t");
                buf.append(cancerStudy.getName() + "\t");
                buf.append(cancerStudy.getDescription() + "\n");
            }
            return buf.toString();
        } else {
            throw new ProtocolException("No cancer studies accessible; either provide credentials to access private studies, " +
                    "or ask administrator to load public ones.\n");
        }
    }

    /**
    * Get user credentials for given email address.
	 *
     * @param email
     * @return Table output.
     * @throws DaoException Database Error.
	 *
	 * Additional level of method security.  Only users with ROLE_PORTAL|_ADMIN
	 * and a principal object of type ConsumerDetails (meaning we got here through OAUTH)
	 * are allowed to access this method.  @PreAuthorized is evaluated before method is 
	 * allowed to execute.
	*/
	@PreAuthorize("hasRole('ROLE_PORTAL_ADMIN') and " +
				  "principal instanceof T(org.springframework.security.oauth.provider.ConsumerDetails)")
    public String getUserCredentials(String email) throws DaoException {

		if (log.isDebugEnabled()) {
			log.debug("email: " + email);
		}

		User user = DaoUser.getUserByEmail(email);
        StringBuffer buf = new StringBuffer();
        if (user != null && user.isEnabled()) {
			UserAuthorities userAuthorities = DaoUserAuthorities.getUserAuthorities(user);
			buf.append(user.getConsumerSecret() + "\t");
			for (String authority : userAuthorities.getAuthorities()) {
				buf.append(authority + ",");
			}
			buf.deleteCharAt(buf.length()-1);
			buf.append("\n");
		}
		else {
			buf.append("Error:  Unknown user or account disabled:  " + email + ".\n");
        }

		if (log.isDebugEnabled()) {
			log.debug("buffer: " + buf.toString());
		}

		// outta here
        return buf.toString();
    }

    /**
     * Gets Cancer Studies.
     *
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
	 *
	 * Method level security locks down this method call.  Only users with ROLE_USER
	 * and a principal object of type ConsumerDetails (meaning we got here through OAUTH)
	 * are allowed to access this method.  @PreAuthorized is evaluated before method is 
	 * allowed to execute.
     */
	@PreAuthorize("hasRole('ROLE_USER') and " +
				  "principal instanceof T(org.springframework.security.oauth.provider.ConsumerDetails)")
    public String getCancerStudies() throws DaoException, ProtocolException {

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), getting accessible cancer studies.");
		}

		// get list of accessible cancer studies
        List<CancerStudy> accessibleCancerStudies = getAccessibleCancerStudies();

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), number of accessibleCancerStudies: " + accessibleCancerStudies.size());
		}

        StringBuffer buf = new StringBuffer();
        if (accessibleCancerStudies.size() > 0) {

            buf.append("cancer_study_id\tname\tdescription\n");
            for (CancerStudy cancerStudy : accessibleCancerStudies) {
                
                // changed to output stable identifier, instead of internal integer identifer.
                buf.append(cancerStudy.getCancerStudyStableId() + "\t");
                buf.append(cancerStudy.getName() + "\t");
                buf.append(cancerStudy.getDescription() + "\n");
            }
            return buf.toString();
        } else {
            throw new ProtocolException("No cancer studies accessible; either provide credentials to access private studies, " +
                    "or ask administrator to load public ones.\n");
        }
    }

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return boolean
     * @throws DaoException
     */
    public boolean checkAccess(String stableStudyId) throws DaoException {

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(stableStudyId);
		if (log.isDebugEnabled()) {
			log.debug("checkAccess(), stableStudyId: " + stableStudyId);
		}

		// outta here
		return checkAccess(cancerStudy);
	}

	/**
	 * Helper function that provides list of accessible cancer studies.
	 *
	 * @return List<CancerStudy>
	 * @throws DaoException
	 *
	 * We use @PostFilter annotation to remove elements
	 * in the return list inaccessible to the user.
	 */
	@PostFilter("hasPermission(filterObject, 'read')")
	private List<CancerStudy> getAccessibleCancerStudies() throws DaoException {
		return DaoCancerStudy.getAllCancerStudies();
	}

	/**
	 * Helper function that provides authorization on given cancer study.
	 *
	 * @return boolean 
	 *
	 * We use @PreAuthorize annotation to provide 
	 * permission evaluation on this cancer study.
	 */
	@PreAuthorize("hasPermission(#cancerStudy, 'read')")
	private boolean checkAccess(CancerStudy cancerStudy) {
		return true;
	}
}

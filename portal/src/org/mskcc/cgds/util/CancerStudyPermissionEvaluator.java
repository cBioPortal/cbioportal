// package
package org.mskcc.cgds.util;

// imports
import org.mskcc.cgds.model.CancerStudy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.PermissionEvaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * A custom PermissionEvaluator implementation that checks whether a
 * particular user has access to a particular cancer study.
 *
 * Anonymous users will only get access to public studies.
 *
 * @author Benjamin Gross
 */
class CancerStudyPermissionEvaluator implements PermissionEvaluator {

	// ref to log
	private static Log log = LogFactory.getLog(CancerStudyPermissionEvaluator.class);

    /**
	 * Implementation of {@code PermissionEvaluator}.
	 * We do not support this method call.
	 */
	public boolean hasPermission(Authentication authentication, Serializable targetId,
								 String targetType, Object permission) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Implementation of {@code PermissionEvaluator}.
	 */
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

		CancerStudy cancerStudy = (CancerStudy)targetDomainObject;

		if (log.isDebugEnabled()) {
			if (cancerStudy == null) {
				log.debug("hasPermission(), cancer study is null.");
			}
			else {
				log.debug("hasPermission(), cancer study id: " + cancerStudy.getStudyId());
			}
            if (authentication == null) {
                log.debug("hasPermission(), authentication is null, " +
                          "permission granted only if cancer study is public.");
            }
		}

        // nothing to do if cancer study is null,
        // return false as spring-security document specifies
		if (cancerStudy == null) {
			return false;
		}

        boolean publicStudy = cancerStudy.isPublicStudy();
        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), public study: " + publicStudy);
        }

        // if public study or
        // public study and authentication is null (anonymous user)
        // bypass granted authorities check
        if (publicStudy || (publicStudy && authentication == null)) {
            return true;
        }
        // private study and anonymous user does not get permission
        else if (!publicStudy && authentication == null) {
            return false;
        }

		if (log.isDebugEnabled()) {
			UserDetails userDetails = (UserDetails)authentication.getPrincipal();
			log.debug("hasPermission(), username: " + userDetails.getUsername());
		}

        // does UserAccessRight contain user, studyId?
        // - compare this cancer study with authorities

		// outta here
		return true;
	}
}




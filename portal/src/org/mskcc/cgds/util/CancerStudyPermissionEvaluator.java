// package
package org.mskcc.cgds.util;

// imports
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.dao.DaoUserAccessRight;

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
 * particular user has access to a particular cancer study.  Current,
 * we just use DaoUserAccessRight.  We should probably change to true ACL
 * in the future.
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
		}

		if (cancerStudy == null) {
			return false;
		}

		if (log.isDebugEnabled()) {
			UserDetails userDetails = (UserDetails)authentication.getPrincipal();
			log.debug("username: " + userDetails.getUsername());
			log.debug("hasPermission(), public study: " + cancerStudy.isPublicStudy());
		}

        // does UserAccessRight contain user, studyId?
        //return DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy(email, cancerStudy.getStudyId());

		// outta here
		return true;
	}
}




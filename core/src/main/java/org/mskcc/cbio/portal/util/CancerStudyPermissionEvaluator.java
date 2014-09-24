/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

// package
package org.mskcc.cbio.portal.util;

// imports
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.social.security.SocialUser;


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
        @Override
	public boolean hasPermission(Authentication authentication, Serializable targetId,
								 String targetType, Object permission) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Implementation of {@code PermissionEvaluator}.
	 */
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

		if (GlobalProperties.usersMustBeAuthorized()) {

			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), authorization is true, checking permissions...");
			}

			CancerStudy cancerStudy = null;
			if (targetDomainObject instanceof CancerStudy) {
				cancerStudy = ((CancerStudy)targetDomainObject);
			}

			if (log.isDebugEnabled()) {
				if (cancerStudy == null) {
					log.debug("hasPermission(), stable cancer study ID is null.");
				} 
				if (authentication == null) {
					log.debug("hasPermission(), authentication is null.");
				}
			}

			// nothing to do if stable cancer study is null or authentication is null
			// return false as spring-security document specifies
			if (cancerStudy == null || authentication == null) {
				return false;
			}

			SocialUser socialUser = (SocialUser) authentication.getPrincipal();
			if (socialUser != null && socialUser instanceof SocialUser) {
				return hasPermission(cancerStudy, socialUser);
			}
			else {
				return false;
			}
		}		else {
			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), authorization is false, returning true...");
			}
			return true;
		}
	}

	/**
	 * Helper function to determine if given user has access to given cancer study.
	 *
	 * @param stableStudyID String
	 * @param user SocialUserDetails
	 * @return boolean
	 */
	private boolean hasPermission(CancerStudy cancerStudy, SocialUser user) {

		/*
		  boolean publicStudy = cancerStudy.isPublicStudy();
		  if (log.isDebugEnabled()) {
		  log.debug("hasPermission(), public study: " + publicStudy);
		  }

		  // if public study or
		  // public study and authentication is null (anonymous user)
		  // bypass granted authorities check
		  if (publicStudy || (publicStudy && authentication == null)) {
		  return true;
		  // private study and anonymous user does not get permission
		  } else if (!publicStudy && authentication == null) {
		  return false;
		  }
		*/

		//Set<String> grantedAuthorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
                Set<String> grantedAuthorities = getGrantedAuthorities(user);
                
                String stableStudyID = cancerStudy.getCancerStudyStableId();

		if (log.isDebugEnabled()) {
			log.debug("hasPermission(), cancer study stable id: " + stableStudyID);
			log.debug("hasPermission(), user: " + user.getUsername());
			for (String authority : grantedAuthorities) {
				log.debug("hasPermission(), authority: " + authority);
			}
		}

		// a user has permission to access the 'all' cancer study (everybody does)
		if (stableStudyID.equalsIgnoreCase(AccessControl.ALL_CANCER_STUDIES_ID)) {
			return true;
		}
		// if a user has access to 'all', simply return true
		if (grantedAuthorities.contains(AccessControl.ALL_CANCER_STUDIES_ID.toUpperCase())) {
			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), user has access to ALL cancer studies, return true");
			}
			return true;
		}
		// if a user has access to 'all_tcga', simply return true for tcga studies
		if (grantedAuthorities.contains(AccessControl.ALL_TCGA_CANCER_STUDIES_ID.toUpperCase()) &&
			stableStudyID.toUpperCase().endsWith("_TCGA")) {
			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), user has access to ALL_TCGA cancer studies return true");
			}
			return true;
		}
		// if a user has access to 'all_target', simply return true for target studies
		if (grantedAuthorities.contains(AccessControl.ALL_TARGET_CANCER_STUDIES_ID.toUpperCase()) &&
			(stableStudyID.toUpperCase().endsWith("_TARGET")
                         || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE1")
                         || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE2"))) {
			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), user has access to ALL_NCI_TARGET cancer studies return true");
			}
			return true;
		}
                
                // for groups
				Set<String> groups = Collections.emptySet();
				try {
                	groups = cancerStudy.getFreshGroups();
                }
                catch (DaoException e) {
					groups = cancerStudy.getGroups();
                }
                if (!Collections.disjoint(groups, grantedAuthorities)) {
			if (log.isDebugEnabled()) {
				log.debug("hasPermission(), user has access by groups return true");
			}
			return true;
                }

		boolean toReturn = grantedAuthorities.contains(stableStudyID.toUpperCase());

		if (log.isDebugEnabled()) {
			if (toReturn == true) {
				log.debug("hasPermission(), user has access to this cancer study: '" + stableStudyID + "', returning true.");
			}
			else {
				log.debug("hasPermission(), user does not have access to the cancer study: '" + stableStudyID + "', returning false.");
			}
		}

		// outta here
		return toReturn;
	}
        
        private Set<String> getGrantedAuthorities(SocialUser user) {
            String appName = GlobalProperties.getAppName().toUpperCase();
            Set<String> allAuthorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
            Set<String> grantedAuthorities = new HashSet<>();
            for (String au : allAuthorities) {
                if (au.toUpperCase().startsWith(appName+":")) {
                    grantedAuthorities.add(au.substring(appName.length()+1));
                }
            }
            return grantedAuthorities;
        }
}




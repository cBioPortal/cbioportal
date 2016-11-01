/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
                cancerStudy = ((CancerStudy) targetDomainObject);
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

            // Actually, it's not entirely clear why we do this null test, since the
            // authentication will always have authorities.
            Object user = authentication.getPrincipal();
            if (user != null) {
                return hasPermission(cancerStudy, authentication);
            } else {
                return false;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), authorization is false, returning true...");
            }
            return true;
        }
    }

    /**
     * Helper function to determine if given user has access to given cancer study.
     *
     * @param cancerStudy String ID of the cancer study to check for
     * @param user Spring Authentication of the logged-in user.
     * @return boolean
     */
    private boolean hasPermission(CancerStudy cancerStudy, Authentication authentication) {

        Set<String> grantedAuthorities = getGrantedAuthorities(authentication);

        String stableStudyID = cancerStudy.getCancerStudyStableId();

        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), cancer study stable id: " + stableStudyID);
            log.debug("hasPermission(), user: " + authentication.getPrincipal().toString());
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
        } catch (DaoException e) {
            groups = cancerStudy.getGroups();
        }
        if (!Collections.disjoint(groups, grantedAuthorities)) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), user has access by groups return true");
            }
            return true;
        }

        // finally, check if the user has this study specifically listed in his 'groups' (a 'group' of this study only)
        boolean toReturn = grantedAuthorities.contains(stableStudyID.toUpperCase());

        if (log.isDebugEnabled()) {
            if (toReturn == true) {
                log.debug("hasPermission(), user has access to this cancer study: '" + stableStudyID.toUpperCase() + "', returning true.");
            } else {
                log.debug("hasPermission(), user does not have access to the cancer study: '" + stableStudyID.toUpperCase() + "', returning false.");
            }
        }

        // outta here
        return toReturn;
    }

    private Set<String> getGrantedAuthorities(Authentication authentication) {
        String appName = GlobalProperties.getAppName().toUpperCase();
        Set<String> allAuthorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        Set<String> grantedAuthorities = new HashSet<>();

        if (GlobalProperties.filterGroupsByAppName()) {
            for (String au : allAuthorities) {
                if (au.toUpperCase().startsWith(appName + ":")) {
                    grantedAuthorities.add(au.substring(appName.length() + 1).toUpperCase());
                }
            }
        } else {
            for (String au : allAuthorities) {
                grantedAuthorities.add(au.toUpperCase());
            }
        }

        // all users are allowed access to PUBLIC studies
        if (AccessControl.PUBLIC_CANCER_STUDIES_GROUP!=null) {
            grantedAuthorities.add(AccessControl.PUBLIC_CANCER_STUDIES_GROUP.toUpperCase());
        }

        return grantedAuthorities;
    }
}




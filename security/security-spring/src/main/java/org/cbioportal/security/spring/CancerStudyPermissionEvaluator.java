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

package org.cbioportal.security.spring;

// imports
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.persistence.StudyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static Log log = LogFactory.getLog(CancerStudyPermissionEvaluator.class);

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private GeneticProfileRepository geneticProfileRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private SampleListRepository sampleListRepository;

    @Value("${app.name:}")
    private String APP_NAME;
    private String DEFAULT_APP_NAME = "public_portal";

    @Value("${filter_groups_by_appname:true}")
    private String FILTER_GROUPS_BY_APP_NAME;

    private static String PUBLIC_CANCER_STUDIES_GROUP;
    @Value("${always_show_study_group:}")
    private void setPublicCancerStudiesGroup(String property) { 
        PUBLIC_CANCER_STUDIES_GROUP = property; 
        if (log.isDebugEnabled()) {
            log.debug("setPublicCancerStudiesGroup(), always_show_study_group = " + ((property == null) ? "null" : property));
        }
        if (property != null && property.trim().isEmpty()) {
            PUBLIC_CANCER_STUDIES_GROUP = null;
        } 
    }
 
    private static final String ALL_CANCER_STUDIES_ID = "all";
    private static final String ALL_TCGA_CANCER_STUDIES_ID = "all_tcga";
    private static final String ALL_TARGET_CANCER_STUDIES_ID = "all_nci_target";
    private static final String MULTIPLE_CANCER_STUDIES_ID = "multiple";

    /**
     * Implementation of {@code PermissionEvaluator}.
     *
     * @param authentication
     * @param targetId Serialized String cancer study id, 
     *   String genetic profile id, 
     *   String sample list id, 
     *   List<String> of cancer study ids, 
     *   List<String> of genetic profile ids,
     * @param targetType String 'CancerStudy', 
     *   'SampleList', 
     *   'GeneticProfile', 
     *   'SampleList',
     *   'List<CancerStudyId>', 
     *   'List<GeneticProfileId>', 
     * @param permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), checking permissions on targetId");
        }
        if (targetId == null) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), targetId is null, returning false");
            }
            return false;
        }

        if ("CancerStudy".equals(targetType)) {
            // everybody has access the 'all' cancer study
            // we have to check this right here (instead of checking later)
            // because the 'all' cancer study does not exist in the database
            if (targetId.toString().equalsIgnoreCase(ALL_CANCER_STUDIES_ID)) {
                return true;
            }
            CancerStudy cancerStudy = studyRepository.getStudy(targetId.toString());
            if (cancerStudy == null) { 
                return false;
            }
            return hasPermission(authentication, cancerStudy, permission);
        } else if ("GeneticProfile".equals(targetType)) {
            GeneticProfile geneticProfile = geneticProfileRepository.getGeneticProfile(targetId.toString());
            if (geneticProfile == null) {
                return false;
            }
            return hasPermission(authentication, geneticProfile, permission);
        } else if ("SampleList".equals(targetType)) {
            SampleList sampleList = sampleListRepository.getSampleList(targetId.toString());
            if (sampleList == null) {
                return false;
            }
            return hasPermission(authentication, sampleList, permission);
        }  else if ("List<CancerStudyId>".equals(targetType)) {
            List<String> studyIds = (List<String>) targetId;
            for (String studyId : studyIds) {
                if (!hasPermission(authentication, studyId, "CancerStudy", permission)) {
                    return false;
                }
            }
            return true;
        } else if ("List<GeneticProfileId>".equals(targetType)) {
            List<String> geneticProfileIds = (List<String>) targetId;
            for (String geneticProfileId : geneticProfileIds) {
                GeneticProfile geneticProfile = geneticProfileRepository.getGeneticProfile(geneticProfileId);
                if (geneticProfile == null || !hasPermission(authentication, geneticProfile, permission)) {
                    return false;
                }
            }
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), unknown targetType '" + targetType + "'");
            }
        }
        return false;
    }

    /**
     * Implementation of {@code PermissionEvaluator}.
     *
     * @param authentication
     * @param targetDomainObject CancerStudy, GeneticProfile, or SampleList
     * @param permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), checking permissions on targetDomainObject");
        }
        if (targetDomainObject == null) {
           if (log.isDebugEnabled()) {
                log.debug("hasPermission(), targetDomainObject is null, returning false");
            }
            return false;
        }

        CancerStudy cancerStudy = null;
        if (targetDomainObject instanceof CancerStudy) {
            cancerStudy = (CancerStudy) targetDomainObject;
        } else if (targetDomainObject instanceof GeneticProfile) {
            cancerStudy = ((GeneticProfile) targetDomainObject).getCancerStudy(); 
            if (cancerStudy == null) {
                // cancer study was not included so get it
                cancerStudy = studyRepository.getStudy(((GeneticProfile) targetDomainObject).getCancerStudyIdentifier());
            }
        } else if (targetDomainObject instanceof SampleList) {
            cancerStudy = ((SampleList) targetDomainObject).getCancerStudy();
            if (cancerStudy == null) {
                // cancer study was not included so get it
                cancerStudy = studyRepository.getStudy(((SampleList) targetDomainObject).getCancerStudyIdentifier());
            }
        } else { 
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), targetDomainObject class is '" + targetDomainObject.getClass().getName() + "'");
            }
        }

        if (log.isDebugEnabled()) {
            if (cancerStudy == null) {
                log.debug("hasPermission(), stable cancer study is null.");
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
    }

    /**
     * Helper function to determine if given user has access to given cancer study.
     *
     * @param cancerStudy cancer study to check for
     * @param user Spring Authentication of the logged-in user.
     * @return boolean
     */
    private boolean hasPermission(CancerStudy cancerStudy, Authentication authentication) {

        Set<String> grantedAuthorities = getGrantedAuthorities(authentication);

        String stableStudyID = cancerStudy.getCancerStudyIdentifier();

        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), cancer study stable id: " + stableStudyID);
            log.debug("hasPermission(), user: " + authentication.getPrincipal().toString());
            for (String authority : grantedAuthorities) {
                log.debug("hasPermission(), authority: " + authority);
            }
        }

        // everybody has access the 'all' cancer study
        if (stableStudyID.equalsIgnoreCase(ALL_CANCER_STUDIES_ID)) {
            return true;
        }
        // if a user has access to 'all', simply return true
        if (grantedAuthorities.contains(ALL_CANCER_STUDIES_ID.toUpperCase())) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), user has access to ALL cancer studies, return true");
            }
            return true;
        }
        // if a user has access to 'all_tcga', simply return true for tcga studies
        if (grantedAuthorities.contains(ALL_TCGA_CANCER_STUDIES_ID.toUpperCase()) &&
                stableStudyID.toUpperCase().endsWith("_TCGA")) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), user has access to ALL_TCGA cancer studies return true");
            }
            return true;
        }
        // if a user has access to 'all_target', simply return true for target studies
        if (grantedAuthorities.contains(ALL_TARGET_CANCER_STUDIES_ID.toUpperCase()) &&
                (stableStudyID.toUpperCase().endsWith("_TARGET")
                        || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE1")
                        || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE2"))) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), user has access to ALL_NCI_TARGET cancer studies return true");
            }
            return true;
        }

        // check if user is in study groups
        // we don't want the groups from the cached cancer study
        Set<String> groups = securityRepository.getCancerStudyGroups(cancerStudy.getCancerStudyId());
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
        String appName = getAppName().toUpperCase();
        Set<String> allAuthorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        Set<String> grantedAuthorities = new HashSet<>();

        if (filterGroupsByAppName()) {
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
        if (log.isDebugEnabled()) {
            log.debug("PUBLIC_CANCER_STUDIES_GROUP= " + ((PUBLIC_CANCER_STUDIES_GROUP == null) ? "null" : PUBLIC_CANCER_STUDIES_GROUP));
        }

        if (PUBLIC_CANCER_STUDIES_GROUP != null) {
            grantedAuthorities.add(PUBLIC_CANCER_STUDIES_GROUP.toUpperCase());
        }

        return grantedAuthorities;
    }

    private String getAppName() {
        if (log.isDebugEnabled()) {
            log.debug("getAppName(), APP_NAME = " + ((APP_NAME == null) ? "null" : APP_NAME));
        }
        return (APP_NAME == null || APP_NAME.trim().isEmpty()) ? DEFAULT_APP_NAME : APP_NAME;
    }

    private boolean filterGroupsByAppName() {
        if (log.isDebugEnabled()) {
            log.debug("filterGroupsByAppName(), FILTER_GROUPS_BY_APP_NAME = " + ((FILTER_GROUPS_BY_APP_NAME == null) ? "null" : FILTER_GROUPS_BY_APP_NAME));
        }
        return FILTER_GROUPS_BY_APP_NAME == null || Boolean.parseBoolean(FILTER_GROUPS_BY_APP_NAME);
    }
}




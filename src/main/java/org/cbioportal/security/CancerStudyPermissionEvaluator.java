/*
 * Copyright (c) 2015 - 2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.security;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SampleList;
import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.utils.security.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CancerStudyPermissionEvaluator implements PermissionEvaluator {

    private final CacheMapUtil cacheMapUtil;

    private static final String ALL_CANCER_STUDIES_ID = "all";
    private static final String ALL_TCGA_CANCER_STUDIES_ID = "all_tcga";
    private static final String ALL_TARGET_CANCER_STUDIES_ID = "all_nci_target";
    private static final String TARGET_TYPE_CANCER_STUDY_ID = "CancerStudyId";
    private static final String TARGET_TYPE_MOLECULAR_PROFILE_ID = "MolecularProfileId";
    private static final String TARGET_TYPE_GENETIC_PROFILE_ID = "GeneticProfileId";
    private static final String TARGET_TYPE_SAMPLE_LIST_ID = "SampleListId";
    private static final String TARGET_TYPE_COLLECTION_OF_SAMPLE_LIST_IDS = "Collection<SampleListId>";
    private static final String TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS = "Collection<CancerStudyId>";
    private static final String TARGET_TYPE_COLLECTION_OF_MOLECULAR_PROFILE_IDS = "Collection<MolecularProfileId>";
    private static final String TARGET_TYPE_COLLECTION_OF_GENETIC_PROFILE_IDS = "Collection<GeneticProfileId>";
    private static final Logger log = LoggerFactory.getLogger(CancerStudyPermissionEvaluator.class);

    private final String APP_NAME;
    private String DEFAULT_APP_NAME = "public_portal";

    private final String FILTER_GROUPS_BY_APP_NAME;

    private final String PUBLIC_CANCER_STUDIES_GROUP;
//    @Value("${always_show_study_group:}")
//    private void setPublicCancerStudiesGroup(String property) {
//        PUBLIC_CANCER_STUDIES_GROUP = property;
//        if (log.isDebugEnabled()) {
//            log.debug("setPublicCancerStudiesGroup(), always_show_study_group = " + ((property == null) ? "null" : property));
//        }
//        if (property != null && property.trim().isEmpty()) {
//            PUBLIC_CANCER_STUDIES_GROUP = null;
//        }
//    }
    
    public CancerStudyPermissionEvaluator(final String appName, final String doFilterGroupsByAppName, final String alwaysShowCancerStudyGroup, final CacheMapUtil cacheMapUtil ) {
        this.APP_NAME = appName;
        this.FILTER_GROUPS_BY_APP_NAME = doFilterGroupsByAppName;
        this.PUBLIC_CANCER_STUDIES_GROUP = alwaysShowCancerStudyGroup;
        this.cacheMapUtil = cacheMapUtil;
    }
    /**
     * Implementation of {@code PermissionEvaluator}.
     * this method handles the direct evaluation of user access to individual instances from the data model.
     *
     * @param authentication
     * @param targetDomainObject CancerStudy, MolecularProfile, SampleList, or Patient
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
        CancerStudy cancerStudy = getRelevantCancerStudyFromTarget(targetDomainObject);
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
            return hasAccessToCancerStudy(authentication, cancerStudy, (AccessLevel)  permission);
        } else {
            return false;
        }
    }

    /**
     * Implementation of {@code PermissionEvaluator}.
     *
     * @param authentication
     * @param targetId Serialized String cancer study id,
     *   String molecular profile id,
     *   String genetic profile id,
     *   String sample list id,
     *   Collection<String> of cancer study ids,
     *   Collection<String> of molecular profile ids,
     *   Collection<String> of genetic profile ids,
     *   or Collection<String> of sample list ids
     * @param targetType String 'CancerStudyId',
     *   'MolecularProfileId',
     *   'GeneticProfileId',
     *   'SampleListId',
     *   'Collection<CancerStudyId>',
     *   'Collection<MolecularProfileId>',
     *   'Collection<GeneticProfileId>',
     *   or 'Collection<SampleListId>'
     * @param permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), checking permissions on targetId");
        }
        if (targetId == null) {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), targetId is null, returning false");
            }
            return false;
        }
        if (TARGET_TYPE_CANCER_STUDY_ID.equals(targetType)) {
            return hasAccessToCancerStudy(authentication, (String)targetId, permission);
        } else if (TARGET_TYPE_MOLECULAR_PROFILE_ID.equals(targetType) || TARGET_TYPE_GENETIC_PROFILE_ID.equals(targetType)) {
            return hasAccessToMolecularProfile(authentication, (String)targetId, permission);
        } else if (TARGET_TYPE_SAMPLE_LIST_ID.equals(targetType)) {
            return hasAccessToSampleList(authentication, (String)targetId, permission);
        } else if (TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS.equals(targetType)) {
            return hasAccessToCancerStudies(authentication, (Collection<String>)targetId, permission);
        } else if (TARGET_TYPE_COLLECTION_OF_MOLECULAR_PROFILE_IDS.equals(targetType) || TARGET_TYPE_COLLECTION_OF_GENETIC_PROFILE_IDS.equals(targetType)) {
            return hasAccessToMolecularProfiles(authentication, (Collection<String>)targetId, permission);
        } else if (TARGET_TYPE_COLLECTION_OF_SAMPLE_LIST_IDS.equals(targetType)) {
            return hasAccessToSampleLists(authentication, (Collection<String>) targetId, permission);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("hasPermission(), unknown targetType '" + targetType + "'");
            }
        }
        return false;
    }

    private CancerStudy getRelevantCancerStudyFromTarget(Object targetDomainObject) {
        if (targetDomainObject instanceof CancerStudy) {
            return (CancerStudy) targetDomainObject;
        } else if (targetDomainObject instanceof MolecularProfile) {
            MolecularProfile molecularProfile = (MolecularProfile) targetDomainObject;
            if (molecularProfile.getCancerStudy() != null) {
                return molecularProfile.getCancerStudy();
            }
            // cancer study was not included so get it from cache
            return cacheMapUtil.getCancerStudyMap().get(molecularProfile.getCancerStudyIdentifier());
        } else if (targetDomainObject instanceof SampleList) {
            SampleList sampleList = (SampleList) targetDomainObject;
            if (sampleList.getCancerStudy() != null) {
                return sampleList.getCancerStudy();
            }
            // cancer study was not included so get it from cache
            return cacheMapUtil.getCancerStudyMap().get(sampleList.getCancerStudyIdentifier());
        } else if (targetDomainObject instanceof Patient) {
            Patient patient = (Patient) targetDomainObject;
            if (patient.getCancerStudy() != null) {
                return patient.getCancerStudy();
            }
            // cancer study was not included so get it from cache
            return cacheMapUtil.getCancerStudyMap().get(patient.getCancerStudyIdentifier());
        }
        // unable to handle targetDomainObject type
        if (log.isDebugEnabled()) {
            log.debug("hasPermission(), targetDomainObject class is '" + targetDomainObject.getClass().getName() + "'");
        }
        return null;
    }

    /**
     * Helper function to determine if given user has access to given cancer study.
     *
     * @param cancerStudy cancer study to check for
     * @param authentication Spring Authentication of the logged-in user.
     * @return boolean
     */
    private boolean hasAccessToCancerStudy(Authentication authentication, CancerStudy cancerStudy, AccessLevel permission) {

        // The 'list' permission is only requested by the /api/studies endpoint of StudyController. This permission is
        // requested by the Study Overview page when the portal instance is configured to show all studies (with non-
        // authorized study options greyed out), instead of only showing authorized studies.
        // When the 'list' permission is requested, CancerPermissionEvaluator returns true always.
        if (AccessLevel.LIST == permission) {
            return true;
        }

        Set<String> grantedAuthorities = getGrantedAuthorities(authentication);
        String stableStudyID = cancerStudy.getCancerStudyIdentifier();
        if (log.isDebugEnabled()) {
            log.debug("hasAccessToCancerStudy(), cancer study stable id: " + stableStudyID);
            log.debug("hasAccessToCancerStudy(), user: " + authentication.getPrincipal().toString());
            for (String authority : grantedAuthorities) {
                log.debug("hasAccessToCancerStudy(), authority: " + authority);
            }
        }
        // everybody has access the 'all' cancer study
        if (stableStudyID.equalsIgnoreCase(ALL_CANCER_STUDIES_ID)) {
            return true;
        }
        // if a user has access to 'all', simply return true
        if (grantedAuthorities.contains(ALL_CANCER_STUDIES_ID.toUpperCase())) {
            if (log.isDebugEnabled()) {
                log.debug("hasAccessToCancerStudy(), user has access to ALL cancer studies, return true");
            }
            return true;
        }
        // if a user has access to 'all_tcga', simply return true for tcga studies
        if (grantedAuthorities.contains(ALL_TCGA_CANCER_STUDIES_ID.toUpperCase()) &&
                stableStudyID.toUpperCase().endsWith("_TCGA")) {
            if (log.isDebugEnabled()) {
                log.debug("hasAccessToCancerStudy(), user has access to ALL_TCGA cancer studies return true");
            }
            return true;
        }
        // if a user has access to 'all_target', simply return true for target studies
        if (grantedAuthorities.contains(ALL_TARGET_CANCER_STUDIES_ID.toUpperCase()) &&
                (stableStudyID.toUpperCase().endsWith("_TARGET")
                        || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE1")
                        || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE2"))) {
            if (log.isDebugEnabled()) {
                log.debug("hasAccessToCancerStudy(), user has access to ALL_NCI_TARGET cancer studies return true");
            }
            return true;
        }
        // check if user is in study groups
        // performance now takes precedence over group accuracy (minimal risk to caching cancer study groups)
        // need to filter out empty groups, this can cause issue if grantedAuthorities and groups both contain empty string
        Set<String> groups = Arrays.stream(cancerStudy.getGroups().split(";"))
            .filter(g -> !g.isEmpty())
            .collect(Collectors.toSet());
        if (!Collections.disjoint(groups, grantedAuthorities)) {
            if (log.isDebugEnabled()) {
                log.debug("hasAccessToCancerStudy(), user has access by groups return true");
            }
            return true;
        }
        // finally, check if the user has this study specifically listed in his 'groups' (a 'group' of this study only)
        boolean toReturn = grantedAuthorities.contains(stableStudyID.toUpperCase());
        if (log.isDebugEnabled()) {
            if (toReturn == true) {
                log.debug("hasAccessToCancerStudy(), user has access to this cancer study: '" + stableStudyID.toUpperCase() + "', returning true.");
            } else {
                log.debug("hasAccessToCancerStudy(), user does not have access to the cancer study: '" + stableStudyID.toUpperCase() + "', returning false.");
            }
        }
        return toReturn;
    }

    private boolean hasAccessToCancerStudy(Authentication authentication, String cancerStudyId, Object permission) {
        // everybody has access the 'all' cancer study
        // we have to check this right here (instead of checking later)
        // because the 'all' cancer study does not exist in the database
        if (cancerStudyId.equalsIgnoreCase(ALL_CANCER_STUDIES_ID)) {
            return true;
        }
        CancerStudy cancerStudy = cacheMapUtil.getCancerStudyMap().get(cancerStudyId);
        if (cancerStudy == null) {
            return false;
        }
        return hasPermission(authentication, cancerStudy, permission);
    }

    private boolean hasAccessToMolecularProfile(Authentication authentication, String molecularProfileId, Object permission) {
        MolecularProfile molecularProfile = cacheMapUtil.getMolecularProfileMap().get(molecularProfileId);
        if (molecularProfile == null) {
            return false;
        }
        return hasPermission(authentication, molecularProfile, permission);
    }

    private boolean hasAccessToSampleList(Authentication authentication, String sampleListId, Object permission) {
        SampleList sampleList = cacheMapUtil.getSampleListMap().get(sampleListId);
        if (sampleList == null) {
            return false;
        }
        return hasPermission(authentication, sampleList, permission);
    }

    private boolean hasAccessToCancerStudies(Authentication authentication, Collection<String> cancerStudyIds, Object permission) {
        for (String cancerStudyId : cancerStudyIds) {
            if (!hasPermission(authentication, cancerStudyId, TARGET_TYPE_CANCER_STUDY_ID, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAccessToMolecularProfiles(Authentication authentication, Collection<String> molecularProfileIds, Object permission) {
        for (String molecularProfileId : molecularProfileIds) {
            MolecularProfile molecularProfile = cacheMapUtil.getMolecularProfileMap().get(molecularProfileId);
            if (molecularProfile == null || !hasPermission(authentication, molecularProfile, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAccessToSampleLists(Authentication authentication, Collection<String> sampleListIds, Object permission) {
        for (String sampleListId : sampleListIds) {
            SampleList sampleList = cacheMapUtil.getSampleListMap().get(sampleListId);
            if (sampleList == null || !hasPermission(authentication, sampleList, permission)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> getGrantedAuthorities(Authentication authentication) {
        String appName = getAppName().toUpperCase();
        // need to filter out empty authorities, this can cause issue if grantedAuthorities and groups both contain empty string
        Set<String> allAuthorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities())
            .stream()
            .map(authority -> authority.replaceAll("^ROLE_", ""))
            .filter(a -> !a.isEmpty())
            .collect(Collectors.toSet());
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
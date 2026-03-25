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

package org.cbioportal.application.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.application.security.util.CancerStudyExtractorUtil;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.legacy.web.parameter.DataBinCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataCountFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * A custom PermissionEvaluator implementation that checks whether a particular user has access to a
 * particular cancer study.
 *
 * <p>Anonymous users will only get access to public studies.
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
  private static final String TARGET_TYPE_COLLECTION_OF_SAMPLE_LIST_IDS =
      "Collection<SampleListId>";
  private static final String TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS =
      "Collection<CancerStudyId>";
  private static final String TARGET_TYPE_COLLECTION_OF_MOLECULAR_PROFILE_IDS =
      "Collection<MolecularProfileId>";
  private static final String TARGET_TYPE_COLLECTION_OF_GENETIC_PROFILE_IDS =
      "Collection<GeneticProfileId>";
  private static final Logger log = LoggerFactory.getLogger(CancerStudyPermissionEvaluator.class);

  private final String APP_NAME;
  private String DEFAULT_APP_NAME = "public_portal";

  private final String FILTER_GROUPS_BY_APP_NAME;

  private final String PUBLIC_CANCER_STUDIES_GROUP;

  //    @Value("${always_show_study_group:}")
  //    private void setPublicCancerStudiesGroup(String property) {
  //        PUBLIC_CANCER_STUDIES_GROUP = property;
  //        if (log.isDebugEnabled()) {
  //            log.debug("setPublicCancerStudiesGroup(), always_show_study_group = " + ((property
  // == null) ? "null" : property));
  //        }
  //        if (property != null && property.trim().isEmpty()) {
  //            PUBLIC_CANCER_STUDIES_GROUP = null;
  //        }
  //    }

  public CancerStudyPermissionEvaluator(
      final String appName,
      final String doFilterGroupsByAppName,
      final String alwaysShowCancerStudyGroup,
      final CacheMapUtil cacheMapUtil) {
    this.APP_NAME = appName;
    this.FILTER_GROUPS_BY_APP_NAME = doFilterGroupsByAppName;
    this.PUBLIC_CANCER_STUDIES_GROUP = alwaysShowCancerStudyGroup;
    this.cacheMapUtil = cacheMapUtil;
  }

  /**
   * Implementation of {@code PermissionEvaluator}. this method handles the direct evaluation of
   * user access to individual instances from the data model.
   *
   * @param authentication
   * @param targetDomainObject CancerStudy, MolecularProfile, SampleList, or Patient
   * @param permission
   */
  @Override
  public boolean hasPermission(
      Authentication authentication, Object targetDomainObject, Object permission) {
    if (log.isDebugEnabled()) {
      log.debug("hasPermission(), checking permissions on targetDomainObject");
    }
    if (targetDomainObject == null) {
      if (log.isDebugEnabled()) {
        log.debug("hasPermission(), targetDomainObject is null, returning false");
      }
      return false;
    }
    CancerStudy cancerStudy = extractCancerStudy(targetDomainObject);
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
      return hasAccessToCancerStudy(authentication, cancerStudy, (AccessLevel) permission);
    } else {
      return false;
    }
  }

  /**
   * Implementation of {@code PermissionEvaluator}.
   *
   * @param authentication
   * @param targetId Serialized String cancer study id, String molecular profile id, String genetic
   *     profile id, String sample list id, Collection<String> of cancer study ids,
   *     Collection<String> of molecular profile ids, Collection<String> of genetic profile ids, or
   *     Collection<String> of sample list ids
   * @param targetType String 'CancerStudyId', 'MolecularProfileId', 'GeneticProfileId',
   *     'SampleListId', 'Collection<CancerStudyId>', 'Collection<MolecularProfileId>',
   *     'Collection<GeneticProfileId>', or 'Collection<SampleListId>'
   * @param permission
   */
  @Override
  public boolean hasPermission(
      Authentication authentication, Serializable targetId, String targetType, Object permission) {
    if (log.isDebugEnabled()) {
      log.debug("hasPermission(), checking permissions on targetId");
    }
    if (targetId == null) {
      if (log.isDebugEnabled()) {
        log.debug("hasPermission(), targetId is null, returning false");
      }
      return false;
    }

    try {
      Collection<CancerStudy> cancerStudies = extractCancerStudiesFromTarget(targetId, targetType);
      for (CancerStudy cs : cancerStudies) {
        if (cs == null || !hasAccessToCancerStudy(authentication, cs, (AccessLevel) permission)) {
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the associated {@code CancerStudy} from a given domain object. Access is always
   * resolved to the CancerStudy, regardless of the target type.
   *
   * @param target The domain object instance. Expected to be one of {@code CancerStudy}, {@code
   *     MolecularProfile}, {@code SampleList}, or {@code Patient}.
   * @return The associated {@code CancerStudy} object, or {@code null} if the study cannot be found
   *     or the target type is not supported.
   */
  private CancerStudy extractCancerStudy(Object target) {
    CancerStudy extractedCancerStudy = null;
    switch (target) {
      case CancerStudy cs -> extractedCancerStudy = cs;
      case MolecularProfile mp -> {
        var cs = mp.getCancerStudy();
        if (mp.getCancerStudy() == null) {
          cs = cacheMapUtil.getCancerStudyMap().get(mp.getCancerStudyIdentifier());
        }
        extractedCancerStudy = cs;
      }
      case SampleList sl -> {
        var cs = sl.getCancerStudy();
        if (cs == null) {
          cs = cacheMapUtil.getCancerStudyMap().get(sl.getCancerStudyIdentifier());
        }
        extractedCancerStudy = cs;
      }
      case Patient p -> {
        var cs = p.getCancerStudy();
        if (cs == null) {
          cs = cacheMapUtil.getCancerStudyMap().get(p.getCancerStudyIdentifier());
        }
        extractedCancerStudy = cs;
      }
      case CancerStudyMetadata csm -> {
        extractedCancerStudy = cacheMapUtil.getCancerStudyMap().get(csm.cancerStudyIdentifier());
      }

      default ->
          log.debug("hasPermission(), unknown targetType '" + target.getClass().getName() + "'");
    }
    return extractedCancerStudy;
  }

  /**
   * Converts a target ID/collection/filter into a set of unique {@code CancerStudy} objects.
   *
   * @param target The target object, which can be a single ID, a collection of IDs, or a filter
   *     object.
   * @param targetType The string indicating the type of the target (e.g., 'MolecularProfileId',
   *     'Collection<SampleListId>').
   * @return A {@code Set} of associated {@code CancerStudy} objects. Returns an empty set if the
   *     target is null or unknown.
   * @throws ClassCastException if a collection target cannot be cast to {@code Collection<String>}.
   */
  private Set<CancerStudy> extractCancerStudiesFromTarget(Object target, String targetType) {
    if (target == null) {
      return new HashSet<>();
    }

    // Handle Filter Types
    if (targetType.contains("Filter")) {
      Set<String> studyIds = extractStudyIdsFromFilter(target);
      return extractCancerStudiesFromIds(studyIds, TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS);
    }

    // Handle Collections
    if (target instanceof Collection<?> collection) {
      Collection<String> ids = (Collection<String>) collection;
      return extractCancerStudiesFromIds(ids, targetType);
    }

    if (target instanceof String id) {
      CancerStudy study = extractCancerStudyById(id, targetType);
      return study != null ? Set.of(study) : new HashSet<>();
    }

    log.debug(
        "hasPermission(targetId, targetType), unknown type '" + target.getClass().getName() + "'");
    return new HashSet<>();
  }

  /**
   * Resolves a single ID string of a specific type to its associated {@code CancerStudy}.
   *
   * @param id The string identifier.
   * @param targetType The type of the ID (e.g., {@code TARGET_TYPE_MOLECULAR_PROFILE_ID}).
   * @return The associated {@code CancerStudy}, or {@code null} if not found or type is unknown.
   */
  private CancerStudy extractCancerStudyById(String id, String targetType) {
    return switch (targetType) {
      case TARGET_TYPE_CANCER_STUDY_ID -> cacheMapUtil.getCancerStudyMap().get(id);
      case TARGET_TYPE_MOLECULAR_PROFILE_ID, TARGET_TYPE_GENETIC_PROFILE_ID -> {
        var mp = cacheMapUtil.getMolecularProfileMap().get(id);
        yield extractCancerStudy(mp);
      }
      case TARGET_TYPE_SAMPLE_LIST_ID -> {
        var sampleList = cacheMapUtil.getSampleListMap().get(id);
        yield extractCancerStudy(sampleList);
      }
      default -> {
        log.debug("hasPermission(), unknown targetType '" + targetType + "'");
        yield null;
      }
    };
  }

  /**
   * Converts a collection of identifiers (of a specific type) into a set of unique {@code
   * CancerStudy} objects.
   *
   * @param ids The collection of ID strings.
   * @param targetType The type of the collection (e.g., {@code
   *     TARGET_TYPE_COLLECTION_OF_MOLECULAR_PROFILE_IDS}).
   * @return A {@code Set<CancerStudy>} containing all unique, non-null studies linked to the
   *     provided IDs.
   */
  private Set<CancerStudy> extractCancerStudiesFromIds(Collection<String> ids, String targetType) {
    return ids.stream()
        .map(id -> extractCancerStudyById(id, getSingleTargetType(targetType)))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  /**
   * Converts a collection-based target type string into its single-item equivalent. This is used
   * when iterating over a collection of IDs to determine the type of each individual ID.
   *
   * @param collectionTargetType The collection type string (e.g., {@code
   *     TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS}).
   * @return The corresponding single-item type string (e.g., {@code TARGET_TYPE_CANCER_STUDY_ID}).
   */
  private String getSingleTargetType(String collectionTargetType) {
    return switch (collectionTargetType) {
      case TARGET_TYPE_COLLECTION_OF_CANCER_STUDY_IDS -> TARGET_TYPE_CANCER_STUDY_ID;
      case TARGET_TYPE_COLLECTION_OF_MOLECULAR_PROFILE_IDS -> TARGET_TYPE_MOLECULAR_PROFILE_ID;
      case TARGET_TYPE_COLLECTION_OF_GENETIC_PROFILE_IDS -> TARGET_TYPE_GENETIC_PROFILE_ID;
      case TARGET_TYPE_COLLECTION_OF_SAMPLE_LIST_IDS -> TARGET_TYPE_SAMPLE_LIST_ID;
      default -> collectionTargetType;
    };
  }

  /**
   * Extracts a set of {@code CancerStudy} identifiers from various application-specific filter
   * objects.
   *
   * @param filter An object instance of a known filter type (e.g., {@code SampleFilter}, {@code
   *     StudyViewFilter}).
   * @return A {@code Set<String>} of unique cancer study identifiers referenced by the filter.
   *     Returns an empty set for unknown filter types or filters that yield no studies.
   * @throws IllegalStateException if a molecular profile ID is encountered but the corresponding
   *     MolecularProfile object cannot be found.
   */
  private Set<String> extractStudyIdsFromFilter(Object filter) {
    return switch (filter) {
      case SampleFilter sampleFilter ->
          CancerStudyExtractorUtil.extractCancerStudyIdsFromSampleFilter(
                  sampleFilter, this.cacheMapUtil)
              .stream()
              .collect(Collectors.toSet());
      case StudyViewFilter studyViewFilter -> studyViewFilter.getUniqueStudyIds();
      case ClinicalDataCountFilter clinicalDataCountFilter ->
          clinicalDataCountFilter.getStudyViewFilter() != null
              ? clinicalDataCountFilter.getStudyViewFilter().getUniqueStudyIds()
              : new HashSet<>();
      case DataBinCountFilter dataBinCountFilter ->
          dataBinCountFilter.getStudyViewFilter() != null
              ? dataBinCountFilter.getStudyViewFilter().getUniqueStudyIds()
              : new HashSet<>();
      case GenomicDataCountFilter genomicDataCountFilter ->
          genomicDataCountFilter.getStudyViewFilter() != null
              ? genomicDataCountFilter.getStudyViewFilter().getUniqueStudyIds()
              : new HashSet<>();
      case GenericAssayDataCountFilter genericAssayDataCountFilter ->
          genericAssayDataCountFilter.getStudyViewFilter() != null
              ? genericAssayDataCountFilter.getStudyViewFilter().getUniqueStudyIds()
              : new HashSet<>();
      case MolecularProfileCasesGroupAndAlterationTypeFilter mpgFilter -> {
        Collection<MolecularProfile> molecularProfiles =
            mpgFilter.getMolecularProfileCasesGroupFilter().stream()
                .flatMap(group -> group.getMolecularProfileCaseIdentifiers().stream())
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .map(id -> cacheMapUtil.getMolecularProfileMap().get(id))
                .collect(Collectors.toList());

        if (molecularProfiles.contains(null)) {
          throw new IllegalStateException("MolecularProfile not found ");
        }

        yield molecularProfiles.stream()
            .map(m -> m.getCancerStudyIdentifier())
            .collect(Collectors.toSet());
      }
      default -> {
        log.debug("hasPermission(), unknown filter type: " + filter.getClass().getName());
        yield new HashSet<>();
      }
    };
  }

  /**
   * Helper function to determine if given user has access to given cancer study.
   *
   * @param cancerStudy cancer study to check for
   * @param authentication Spring Authentication of the logged-in user.
   * @return boolean
   */
  private boolean hasAccessToCancerStudy(
      Authentication authentication, CancerStudy cancerStudy, AccessLevel permission) {

    // The 'list' permission is only requested by the /api/studies endpoint of StudyController. This
    // permission is
    // requested by the Study Overview page when the portal instance is configured to show all
    // studies (with non-
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
    if (grantedAuthorities.contains(ALL_TCGA_CANCER_STUDIES_ID.toUpperCase())
        && stableStudyID.toUpperCase().endsWith("_TCGA")) {
      if (log.isDebugEnabled()) {
        log.debug(
            "hasAccessToCancerStudy(), user has access to ALL_TCGA cancer studies return true");
      }
      return true;
    }
    // if a user has access to 'all_target', simply return true for target studies
    if (grantedAuthorities.contains(ALL_TARGET_CANCER_STUDIES_ID.toUpperCase())
        && (stableStudyID.toUpperCase().endsWith("_TARGET")
            || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE1")
            || stableStudyID.equalsIgnoreCase("ALL_TARGET_PHASE2"))) {
      if (log.isDebugEnabled()) {
        log.debug(
            "hasAccessToCancerStudy(), user has access to ALL_NCI_TARGET cancer studies return"
                + " true");
      }
      return true;
    }
    // check if user is in study groups
    // performance now takes precedence over group accuracy (minimal risk to caching cancer study
    // groups)
    // need to filter out empty groups, this can cause issue if grantedAuthorities and groups both
    // contain empty string
    Set<String> groups =
        Arrays.stream(cancerStudy.getGroups().split(";"))
            .filter(g -> !g.isEmpty())
            .collect(Collectors.toSet());
    if (!Collections.disjoint(groups, grantedAuthorities)) {
      if (log.isDebugEnabled()) {
        log.debug("hasAccessToCancerStudy(), user has access by groups return true");
      }
      return true;
    }
    // finally, check if the user has this study specifically listed in his 'groups' (a 'group' of
    // this study only)
    boolean toReturn = grantedAuthorities.contains(stableStudyID.toUpperCase());
    if (log.isDebugEnabled()) {
      if (toReturn == true) {
        log.debug(
            "hasAccessToCancerStudy(), user has access to this cancer study: '"
                + stableStudyID.toUpperCase()
                + "', returning true.");
      } else {
        log.debug(
            "hasAccessToCancerStudy(), user does not have access to the cancer study: '"
                + stableStudyID.toUpperCase()
                + "', returning false.");
      }
    }
    return toReturn;
  }

  private Set<String> getGrantedAuthorities(Authentication authentication) {
    String appName = getAppName().toUpperCase();
    // need to filter out empty authorities, this can cause issue if grantedAuthorities and groups
    // both contain empty string
    Set<String> allAuthorities =
        AuthorityUtils.authorityListToSet(authentication.getAuthorities()).stream()
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
      log.debug(
          "PUBLIC_CANCER_STUDIES_GROUP= "
              + ((PUBLIC_CANCER_STUDIES_GROUP == null) ? "null" : PUBLIC_CANCER_STUDIES_GROUP));
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
      log.debug(
          "filterGroupsByAppName(), FILTER_GROUPS_BY_APP_NAME = "
              + ((FILTER_GROUPS_BY_APP_NAME == null) ? "null" : FILTER_GROUPS_BY_APP_NAME));
    }
    return FILTER_GROUPS_BY_APP_NAME == null || Boolean.parseBoolean(FILTER_GROUPS_BY_APP_NAME);
  }
}

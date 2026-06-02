package org.cbioportal.application.security.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.ClinicalEventAttributeRequest;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileFilter;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.NamespaceAttributeCountFilter;
import org.cbioportal.legacy.web.parameter.NamespaceComparisonFilter;
import org.cbioportal.legacy.web.parameter.NamespaceDataCountFilter;
import org.cbioportal.legacy.web.parameter.PatientFilter;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.cbioportal.legacy.web.parameter.SurvivalRequest;
import org.cbioportal.legacy.web.util.UniqueKeyExtractor;

/**
 * Extracts the set of cancer study ids referenced by the various request filter objects. This logic
 * was previously duplicated in {@code InvolvedCancerStudyExtractorInterceptor}; it now lives here
 * as the single source of truth used by {@link
 * org.cbioportal.application.security.CancerStudyPermissionEvaluator} to authorize {@code
 * hasPermission(#filter, 'XxxFilter', ...)} expressions directly off the {@code @RequestBody}.
 */
public class CancerStudyExtractorUtil {

  private CancerStudyExtractorUtil() {}

  public static Collection<String> extractCancerStudyIdsFromSampleFilter(
      SampleFilter sampleFilter, CacheMapUtil cacheMapUtil) {
    Collection<String> studyIds;

    if (sampleFilter.getSampleListIds() != null) {
      studyIds =
          extractCancerStudyIdsFromSampleListIds(sampleFilter.getSampleListIds(), cacheMapUtil);
    } else if (sampleFilter.getSampleIdentifiers() != null) {
      studyIds = extractCancerStudyIdsFromSampleIdentifiers(sampleFilter.getSampleIdentifiers());
    } else {
      studyIds = UniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys());
    }

    return studyIds;
  }

  public static Collection<String> extractCancerStudyIdsFromSampleListIds(
      List<String> sampleListIds, CacheMapUtil cacheMapUtil) {
    return sampleListIds.stream()
        .map(
            sampleListId ->
                cacheMapUtil.getSampleListMap().get(sampleListId).getCancerStudyIdentifier())
        .distinct()
        .toList();
  }

  public static Collection<String> extractCancerStudyIdsFromSampleIdentifiers(
      Collection<SampleIdentifier> sampleIdentifiers) {
    return sampleIdentifiers.stream().map(SampleIdentifier::getStudyId).distinct().toList();
  }

  public static Collection<String> extractCancerStudyIdsFromMolecularProfileIds(
      Collection<String> molecularProfileIds, CacheMapUtil cacheMapUtil) {
    return molecularProfileIds.stream()
        .map(
            molecularProfileId -> {
              MolecularProfile molecularProfile =
                  cacheMapUtil.getMolecularProfileMap().get(molecularProfileId);
              return molecularProfile.getCancerStudyIdentifier();
            })
        .distinct()
        .toList();
  }

  public static Collection<String> extractCancerStudyIdsFromSampleMolecularIdentifiers(
      List<SampleMolecularIdentifier> sampleMolecularIdentifiers, CacheMapUtil cacheMapUtil) {
    Set<String> molecularProfileIds =
        sampleMolecularIdentifiers.stream()
            .map(SampleMolecularIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());
    return extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, cacheMapUtil);
  }

  public static Set<String> extractCancerStudyIdsFromPatientFilter(PatientFilter patientFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (patientFilter.getPatientIdentifiers() != null) {
      for (PatientIdentifier patientIdentifier : patientFilter.getPatientIdentifiers()) {
        studyIdSet.add(patientIdentifier.getStudyId());
      }
    } else {
      UniqueKeyExtractor.extractUniqueKeys(patientFilter.getUniquePatientKeys(), studyIdSet);
    }
    return studyIdSet;
  }

  public static Set<String> extractCancerStudyIdsFromMolecularProfileFilter(
      MolecularProfileFilter molecularProfileFilter, CacheMapUtil cacheMapUtil) {
    Set<String> studyIdSet = new HashSet<>();
    if (molecularProfileFilter.getStudyIds() != null) {
      studyIdSet.addAll(molecularProfileFilter.getStudyIds());
    } else {
      studyIdSet.addAll(
          extractCancerStudyIdsFromMolecularProfileIds(
              molecularProfileFilter.getMolecularProfileIds(), cacheMapUtil));
    }
    return studyIdSet;
  }

  public static Set<String> extractCancerStudyIdsFromClinicalAttributeCountFilter(
      ClinicalAttributeCountFilter clinicalAttributeCountFilter, CacheMapUtil cacheMapUtil) {
    Set<String> studyIdSet = new HashSet<>();
    if (clinicalAttributeCountFilter.getSampleListId() != null) {
      studyIdSet.addAll(
          extractCancerStudyIdsFromSampleListIds(
              List.of(clinicalAttributeCountFilter.getSampleListId()), cacheMapUtil));
    } else {
      studyIdSet.addAll(
          extractCancerStudyIdsFromSampleIdentifiers(
              clinicalAttributeCountFilter.getSampleIdentifiers()));
    }
    return studyIdSet;
  }

  public static Set<String> extractCancerStudyIdsFromNamespaceAttributeCountFilter(
      NamespaceAttributeCountFilter namespaceAttributeCountFilter) {
    return new HashSet<>(
        extractCancerStudyIdsFromSampleIdentifiers(
            namespaceAttributeCountFilter.getSampleIdentifiers()));
  }

  public static Set<String> extractCancerStudyIdsFromClinicalDataMultiStudyFilter(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter) {
    Set<String> studyIdSet = new HashSet<>();
    for (ClinicalDataIdentifier clinicalDataIdentifier :
        clinicalDataMultiStudyFilter.getIdentifiers()) {
      studyIdSet.add(clinicalDataIdentifier.getStudyId());
    }
    return studyIdSet;
  }

  public static Set<String> extractCancerStudyIdsFromGenePanelDataMultipleStudyFilter(
      GenePanelDataMultipleStudyFilter filter, CacheMapUtil cacheMapUtil) {
    return profileIdsOrSampleMolecularIdentifiers(
        filter.getMolecularProfileIds(), filter.getSampleMolecularIdentifiers(), cacheMapUtil);
  }

  public static Set<String> extractCancerStudyIdsFromMolecularDataMultipleStudyFilter(
      MolecularDataMultipleStudyFilter filter, CacheMapUtil cacheMapUtil) {
    return profileIdsOrSampleMolecularIdentifiers(
        filter.getMolecularProfileIds(), filter.getSampleMolecularIdentifiers(), cacheMapUtil);
  }

  public static Set<String> extractCancerStudyIdsFromGenericAssayDataMultipleStudyFilter(
      GenericAssayDataMultipleStudyFilter filter, CacheMapUtil cacheMapUtil) {
    return profileIdsOrSampleMolecularIdentifiers(
        filter.getMolecularProfileIds(), filter.getSampleMolecularIdentifiers(), cacheMapUtil);
  }

  public static Set<String> extractCancerStudyIdsFromMutationMultipleStudyFilter(
      MutationMultipleStudyFilter filter, CacheMapUtil cacheMapUtil) {
    return profileIdsOrSampleMolecularIdentifiers(
        filter.getMolecularProfileIds(), filter.getSampleMolecularIdentifiers(), cacheMapUtil);
  }

  public static Set<String> extractCancerStudyIdsFromStructuralVariantFilter(
      StructuralVariantFilter structuralVariantFilter, CacheMapUtil cacheMapUtil) {
    Set<String> studyIdSet = new HashSet<>();
    if (structuralVariantFilter.getSampleMolecularIdentifiers() != null) {
      // controller handler will preferentially use SampleMolecularIdentifiers if they are present
      studyIdSet.addAll(
          extractCancerStudyIdsFromSampleMolecularIdentifiers(
              structuralVariantFilter.getSampleMolecularIdentifiers(), cacheMapUtil));
    } else if (structuralVariantFilter.getMolecularProfileIds() != null) {
      // otherwise, handler will use the list of MolecularProfileIds in the filter
      studyIdSet.addAll(
          extractCancerStudyIdsFromMolecularProfileIds(
              structuralVariantFilter.getMolecularProfileIds(), cacheMapUtil));
    }
    return studyIdSet;
  }

  public static Set<String> extractCancerStudyIdsFromGroupFilter(GroupFilter groupFilter) {
    List<SampleIdentifier> sampleIdentifiers =
        groupFilter.getGroups().stream()
            .flatMap(group -> group.getSampleIdentifiers().stream())
            .collect(Collectors.toList());
    return new HashSet<>(extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers));
  }

  public static Set<String> extractCancerStudyIdsFromMolecularProfileCasesGroups(
      Collection<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters,
      CacheMapUtil cacheMapUtil) {
    Set<String> molecularProfileIds =
        molecularProfileCasesGroupFilters.stream()
            .flatMap(group -> group.getMolecularProfileCaseIdentifiers().stream())
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());
    return new HashSet<>(
        extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, cacheMapUtil));
  }

  public static Set<String> extractCancerStudyIdsFromSurvivalRequest(
      SurvivalRequest survivalRequest) {
    return survivalRequest.getPatientIdentifiers().stream()
        .map(PatientIdentifier::getStudyId)
        .collect(Collectors.toSet());
  }

  public static Set<String> extractCancerStudyIdsFromClinicalEventAttributeRequest(
      ClinicalEventAttributeRequest clinicalEventAttributeRequest) {
    return clinicalEventAttributeRequest.getPatientIdentifiers().stream()
        .map(PatientIdentifier::getStudyId)
        .collect(Collectors.toSet());
  }

  public static Set<String> extractCancerStudyIdsFromNamespaceComparisonFilter(
      NamespaceComparisonFilter namespaceComparisonFilter) {
    return new HashSet<>(
        extractCancerStudyIdsFromSampleIdentifiers(
            namespaceComparisonFilter.getSampleIdentifiers()));
  }

  public static Set<String> extractCancerStudyIdsFromNamespaceDataCountFilter(
      NamespaceDataCountFilter namespaceDataCountFilter) {
    if (namespaceDataCountFilter.getStudyViewFilter() != null) {
      return namespaceDataCountFilter.getStudyViewFilter().getUniqueStudyIds();
    }
    return new HashSet<>();
  }

  private static Set<String> profileIdsOrSampleMolecularIdentifiers(
      List<String> molecularProfileIds,
      List<SampleMolecularIdentifier> sampleMolecularIdentifiers,
      CacheMapUtil cacheMapUtil) {
    if (molecularProfileIds != null) {
      return new HashSet<>(
          extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, cacheMapUtil));
    }
    return new HashSet<>(
        extractCancerStudyIdsFromSampleMolecularIdentifiers(
            sampleMolecularIdentifiers, cacheMapUtil));
  }
}

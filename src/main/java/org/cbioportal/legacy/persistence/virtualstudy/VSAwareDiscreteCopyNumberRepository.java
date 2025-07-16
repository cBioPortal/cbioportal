package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateVirtualMoleculaProfileId;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.toStudyAndSampleIdLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.*;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VSAwareDiscreteCopyNumberRepository implements DiscreteCopyNumberRepository {

  private final VirtualStudyService virtualStudyService;
  private final DiscreteCopyNumberRepository discreteCopyNumberRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareDiscreteCopyNumberRepository(
      VirtualStudyService virtualStudyService,
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualStudyService = virtualStudyService;
    this.discreteCopyNumberRepository = discreteCopyNumberRepository;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {
    SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
    return fetchDiscreteCopyNumbersInMolecularProfile(
        molecularProfileId, sampleList.getSampleIds(), entrezGeneIds, alterationTypes, projection);
  }

  @Override
  public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getDiscreteCopyNumbersInMolecularProfileBySampleListId(
                molecularProfileId,
                sampleListId,
                entrezGeneIds,
                alterationTypes,
                Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {
    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (mapping.isEmpty()) {
      return discreteCopyNumberRepository.fetchDiscreteCopyNumbersInMolecularProfile(
          molecularProfileId, sampleIds, entrezGeneIds, alterationTypes, projection);
    }
    Pair<String, String> profileInfo = mapping.get(molecularProfileId);
    String vitualStudyId = profileInfo.getLeft();
    String originalMolecularProfileId = profileInfo.getRight();
    List<StudyScopedId> vStudySamplePairs =
        sampleIds.stream().map(s -> new StudyScopedId(vitualStudyId, s)).toList();
    Map<StudyScopedId, Set<String>> originalSampleIds =
        virtualStudyService.toMaterializedStudySamplePairsMap(vStudySamplePairs);
    Pair<List<String>, List<String>> studyAndSampleIdLists =
        toStudyAndSampleIdLists(originalSampleIds.keySet());
    List<String> materializedSampleIds = studyAndSampleIdLists.getRight();

    return discreteCopyNumberRepository
        .fetchDiscreteCopyNumbersInMolecularProfile(
            originalMolecularProfileId,
            materializedSampleIds,
            entrezGeneIds,
            alterationTypes,
            projection)
        .stream()
        .map(dcn -> virtualizeDiscreteCopyNumber(vitualStudyId, dcn))
        .toList();
  }

  @Override
  public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {
    return molecularProfileIds.stream()
        .flatMap(
            molecularProfileId ->
                fetchDiscreteCopyNumbersInMolecularProfile(
                    molecularProfileId, sampleIds, entrezGeneIds, alterationTypes, projection)
                    .stream())
        .toList();
  }

  @Override
  public List<DiscreteCopyNumberData>
      getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
          List<String> molecularProfileIds,
          List<String> sampleIds,
          List<GeneFilterQuery> geneFilterQuery,
          String projection) {
    List<DiscreteCopyNumberData> result = new ArrayList<>();
    Set<String> molecularProfileIdSet = new HashSet<>(molecularProfileIds);
    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(molecularProfileIdSet);
    Set<String> materializedMolecularProfileIds =
        molecularProfileIdSet.stream()
            .filter(mpid -> !mapping.containsKey(mpid))
            .collect(Collectors.toSet());
    if (!materializedMolecularProfileIds.isEmpty()) {
      List<String> molecularProfileIdsList = new ArrayList<>();
      List<String> sampleIdsList = new ArrayList<>();
      for (int i = 0; i < sampleIds.size(); i++) {
        String sampleId = sampleIds.get(i);
        String molecularProfileId = molecularProfileIds.get(i);
        if (materializedMolecularProfileIds.contains(molecularProfileId)) {
          molecularProfileIdsList.add(molecularProfileId);
          sampleIdsList.add(sampleId);
        }
      }
      result.addAll(
          discreteCopyNumberRepository
              .getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
                  molecularProfileIdsList, sampleIdsList, geneFilterQuery, projection));
    }
    // TODO simplify this code
    if (!mapping.isEmpty()) {
      Map<String, Map<String, Set<String>>> molecularProfileToStudySamplePairs = new HashMap<>();
      for (int i = 0; i < sampleIds.size(); i++) {
        String vsSampleId = sampleIds.get(i);
        String vsMolecularProfileId = molecularProfileIds.get(i);

        Pair<String, String> profileInfo = mapping.get(vsMolecularProfileId);
        if (profileInfo == null) {
          continue; // Skip if the profile is not found in the mapping
        }
        String vitualStudyId = profileInfo.getLeft();
        String originalMolecularProfileId = profileInfo.getRight();
        if (!molecularProfileToStudySamplePairs.containsKey(vitualStudyId)) {
          molecularProfileToStudySamplePairs.put(vitualStudyId, new HashMap<>());
        }
        if (!molecularProfileToStudySamplePairs
            .get(vitualStudyId)
            .containsKey(originalMolecularProfileId)) {
          molecularProfileToStudySamplePairs
              .get(vitualStudyId)
              .put(originalMolecularProfileId, new HashSet<>());
        }
        molecularProfileToStudySamplePairs
            .get(vitualStudyId)
            .get(originalMolecularProfileId)
            .add(vsSampleId);
      }
      for (Map.Entry<String, Map<String, Set<String>>> entry :
          molecularProfileToStudySamplePairs.entrySet()) {
        String vitualStudyId = entry.getKey();
        Map<String, Set<String>> originalSampleIdsMap = entry.getValue();
        for (Map.Entry<String, Set<String>> sampleEntry : originalSampleIdsMap.entrySet()) {
          String originalMolecularProfileId = sampleEntry.getKey();
          Set<String> vsSampleIds = sampleEntry.getValue();
          List<StudyScopedId> vStudySamplePairs =
              vsSampleIds.stream().map(s -> new StudyScopedId(vitualStudyId, s)).toList();
          Map<StudyScopedId, Set<String>> originalSampleIds =
              virtualStudyService.toMaterializedStudySamplePairsMap(vStudySamplePairs);
          Pair<List<String>, List<String>> studyAndSampleIdLists =
              toStudyAndSampleIdLists(originalSampleIds.keySet());
          List<String> materializedSampleIds = studyAndSampleIdLists.getRight();
          result.addAll(
              discreteCopyNumberRepository
                  .getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
                      List.of(originalMolecularProfileId),
                      materializedSampleIds,
                      geneFilterQuery,
                      projection)
                  .stream()
                  .map(dcn -> virtualizeDiscreteCopyNumber(vitualStudyId, dcn))
                  .toList());
        }
      }
    }
    return result;
  }

  private DiscreteCopyNumberData virtualizeDiscreteCopyNumber(
      String vitualStudyId, DiscreteCopyNumberData dcn) {
    DiscreteCopyNumberData virtualDcn = new DiscreteCopyNumberData();
    virtualDcn.setStudyId(vitualStudyId);
    virtualDcn.setSampleId(dcn.getSampleId());
    virtualDcn.setEntrezGeneId(dcn.getEntrezGeneId());
    virtualDcn.setAlteration(dcn.getAlteration());
    virtualDcn.setPatientId(dcn.getPatientId());
    virtualDcn.setMolecularProfileId(
        calculateVirtualMoleculaProfileId(vitualStudyId, dcn.getMolecularProfileId()));
    virtualDcn.setDriverFilter(dcn.getDriverFilter());
    virtualDcn.setDriverFilterAnnotation(dcn.getDriverFilterAnnotation());
    virtualDcn.setDriverTiersFilter(dcn.getDriverTiersFilter());
    virtualDcn.setDriverTiersFilterAnnotation(dcn.getDriverTiersFilterAnnotation());
    virtualDcn.setGene(dcn.getGene());
    return virtualDcn;
  }

  @Override
  public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchDiscreteCopyNumbersInMolecularProfile(
                molecularProfileId, sampleIds, entrezGeneIds, alterationTypes, Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterations) {
    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (mapping.isEmpty()) {
      return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(
          molecularProfileId, sampleIds, entrezGeneIds, alterations);
    }
    Pair<String, String> profileInfo = mapping.get(molecularProfileId);
    String vitualStudyId = profileInfo.getLeft();
    String originalMolecularProfileId = profileInfo.getRight();

    List<String> materializedSampleIds = null;
    if (sampleIds != null && !sampleIds.isEmpty()) {
      List<StudyScopedId> vStudySamplePairs =
          sampleIds.stream().map(s -> new StudyScopedId(vitualStudyId, s)).toList();
      Map<StudyScopedId, Set<String>> originalSampleIds =
          virtualStudyService.toMaterializedStudySamplePairsMap(vStudySamplePairs);
      Pair<List<String>, List<String>> studyAndSampleIdLists =
          toStudyAndSampleIdLists(originalSampleIds.keySet());
      materializedSampleIds = studyAndSampleIdLists.getRight();
    } else {
      Optional<VirtualStudy> virtualStudyOptional =
          virtualStudyService.getVirtualStudyByIdIfExists(vitualStudyId);
      if (virtualStudyOptional.isPresent()) {
        // TODO now we are assigning all samples, but we should probably assign only samples that
        // are in the original molecular profile or at least in the original study
        materializedSampleIds =
            virtualStudyOptional.get().getData().getStudies().stream()
                .flatMap(s -> s.getSamples().stream())
                .toList();
      }
    }

    return discreteCopyNumberRepository
        .getSampleCountByGeneAndAlterationAndSampleIds(
            originalMolecularProfileId, materializedSampleIds, entrezGeneIds, alterations)
        .stream()
        .toList();
  }
}

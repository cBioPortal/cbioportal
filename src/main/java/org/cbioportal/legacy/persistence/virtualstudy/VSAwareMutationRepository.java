package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.MutationCountByPosition;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.MutationRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;

public class VSAwareMutationRepository implements MutationRepository {

  private final VirtualStudyService virtualStudyService;
  private final MutationRepository mutationRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareMutationRepository(
      VirtualStudyService virtualStudyService,
      MutationRepository mutationRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualStudyService = virtualStudyService;
    this.mutationRepository = mutationRepository;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<Mutation> getMutationsInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
    return fetchMutationsInMolecularProfile(
        molecularProfileId,
        sampleList.getSampleIds(),
        entrezGeneIds,
        snpOnly,
        projection,
        pageSize,
        pageNumber,
        sortBy,
        direction);
  }

  @Override
  public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(
      String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds) {
    MutationMeta meta = new MutationMeta();
    List<Mutation> mutations =
        getMutationsInMolecularProfileBySampleListId(
            molecularProfileId,
            sampleListId,
            entrezGeneIds,
            false,
            Projection.ID.name(),
            null,
            null,
            null,
            null);
    meta.setTotalCount(mutations.size());
    Set<String> sampleIds =
        mutations.stream().map(Mutation::getSampleId).collect(Collectors.toSet());
    meta.setSampleCount(sampleIds.size());
    return meta;
  }

  @Override
  public List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return molecularProfileIds.stream()
        .flatMap(
            molecularProfileId ->
                fetchMutationsInMolecularProfile(
                    molecularProfileId,
                    sampleIds,
                    entrezGeneIds,
                    false,
                    projection,
                    pageSize,
                    pageNumber,
                    sortBy,
                    direction)
                    .stream())
        .toList();
  }

  @Override
  public List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<GeneFilterQuery> geneQueries,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    // TODO implement this method
    throw new UnsupportedOperationException("Method not implemented yet");
  }

  @Override
  public MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {
    MutationMeta meta = new MutationMeta();
    List<Mutation> mutations =
        getMutationsInMultipleMolecularProfiles(
            molecularProfileIds,
            sampleIds,
            entrezGeneIds,
            Projection.ID.name(),
            null,
            null,
            null,
            null);
    meta.setTotalCount(mutations.size());
    Set<String> cntSampleIds =
        mutations.stream().map(Mutation::getSampleId).collect(Collectors.toSet());
    meta.setSampleCount(cntSampleIds.size());
    return meta;
  }

  @Override
  public List<Mutation> fetchMutationsInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (mapping.isEmpty()) {
      return mutationRepository.fetchMutationsInMolecularProfile(
          molecularProfileId,
          sampleIds,
          entrezGeneIds,
          snpOnly,
          projection,
          pageSize,
          pageNumber,
          sortBy,
          direction);
    }
    Pair<String, String> profileInfo = mapping.get(molecularProfileId);
    String vitualStudyId = profileInfo.getLeft();
    String originalMolecularProfileId = profileInfo.getRight();
    List<StudyScopedId> vStudySamplePairs =
        sampleIds.stream().map(s -> new StudyScopedId(vitualStudyId, s)).toList();
    Map<StudyScopedId, Set<String>> originalSampleIds =
        virtualStudyService.toMaterializedStudySamplePairsMap(vStudySamplePairs);
    Pair<List<String>, List<String>> studyAndSampleIdLists =
        virtualStudyService.toStudyAndSampleIdLists(originalSampleIds.keySet());
    List<String> materializedSampleIds = studyAndSampleIdLists.getRight();

    return mutationRepository
        .fetchMutationsInMolecularProfile(
            originalMolecularProfileId,
            materializedSampleIds,
            entrezGeneIds,
            snpOnly,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction)
        .stream()
        .map(m -> virtualStudyService.virtualizeMutation(vitualStudyId, m))
        .toList();
  }

  @Override
  public MutationMeta fetchMetaMutationsInMolecularProfile(
      String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds) {
    MutationMeta meta = new MutationMeta();
    List<Mutation> mutations =
        fetchMutationsInMolecularProfile(
            molecularProfileId,
            sampleIds,
            entrezGeneIds,
            false,
            Projection.ID.name(),
            null,
            null,
            null,
            null);
    meta.setTotalCount(mutations.size());
    Set<String> sampleIdsSet =
        mutations.stream().map(Mutation::getSampleId).collect(Collectors.toSet());
    meta.setSampleCount(sampleIdsSet.size());
    return meta;
  }

  @Override
  public MutationCountByPosition getMutationCountByPosition(
      Integer entrezGeneId, Integer proteinPosStart, Integer proteinPosEnd) {
    throw new UnsupportedOperationException("Method not implemented yet");
  }

  @Override
  public GenomicDataCountItem getMutationCountsByType(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String profileType) {
    throw new UnsupportedOperationException("Method not implemented yet");
  }
}

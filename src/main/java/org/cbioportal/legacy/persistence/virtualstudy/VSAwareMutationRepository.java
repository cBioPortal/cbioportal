package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateVirtualMoleculaProfileId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(new HashSet<>(molecularProfileIds));
    List<String> originalMolecularProfileIds =
        molecularProfileIds.stream()
            .map(mpid -> mapping.containsKey(mpid) ? mapping.get(mpid).getLeft() : mpid)
            .toList();
    Map<Pair<String, String>, Integer> positionIndex = new HashMap<>();
    for (int i = 0; i < originalMolecularProfileIds.size(); i++) {
      positionIndex.put(ImmutablePair.of(originalMolecularProfileIds.get(i), sampleIds.get(i)), i);
    }
    return mutationRepository
        .getMutationsInMultipleMolecularProfilesByGeneQueries(
            originalMolecularProfileIds,
            sampleIds,
            geneQueries,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction)
        .stream()
        // TODO what if sample ids mentioned once as virtual molecular profile and once as original?
        // We need to return two mutations in that case
        .map(
            m -> {
              Integer indx =
                  positionIndex.get(ImmutablePair.of(m.getMolecularProfileId(), m.getSampleId()));
              Pair<String, String> vsIdToOriginalMolecularProfileId =
                  mapping.get(molecularProfileIds.get(indx));
              if (vsIdToOriginalMolecularProfileId != null) {
                return virtualizeMutation(vsIdToOriginalMolecularProfileId.getLeft(), m);
              }
              return m;
            })
        .toList();
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
        .map(m -> virtualizeMutation(vitualStudyId, m))
        .toList();
  }

  private Mutation virtualizeMutation(String virtualStudyId, Mutation m) {
    Mutation virtualMutation = new Mutation();
    virtualMutation.setStudyId(virtualStudyId);
    virtualMutation.setMolecularProfileId(
        calculateVirtualMoleculaProfileId(virtualStudyId, m.getMolecularProfileId()));
    virtualMutation.setSampleId(m.getSampleId());
    virtualMutation.setPatientId(m.getPatientId());
    virtualMutation.setEntrezGeneId(m.getEntrezGeneId());
    virtualMutation.setGene(m.getGene());
    virtualMutation.setCenter(m.getCenter());
    virtualMutation.setMutationStatus(m.getMutationStatus());
    virtualMutation.setValidationStatus(m.getValidationStatus());
    virtualMutation.setTumorAltCount(m.getTumorAltCount());
    virtualMutation.setTumorRefCount(m.getTumorRefCount());
    virtualMutation.setNormalAltCount(m.getNormalAltCount());
    virtualMutation.setNormalRefCount(m.getNormalRefCount());
    virtualMutation.setAminoAcidChange(m.getAminoAcidChange());
    virtualMutation.setChr(m.getChr());
    virtualMutation.setStartPosition(m.getStartPosition());
    virtualMutation.setEndPosition(m.getEndPosition());
    virtualMutation.setReferenceAllele(m.getReferenceAllele());
    virtualMutation.setTumorSeqAllele(m.getTumorSeqAllele());
    virtualMutation.setProteinChange(m.getProteinChange());
    virtualMutation.setMutationType(m.getMutationType());
    virtualMutation.setNcbiBuild(m.getNcbiBuild());
    virtualMutation.setVariantType(m.getVariantType());
    virtualMutation.setRefseqMrnaId(m.getRefseqMrnaId());
    virtualMutation.setProteinPosStart(m.getProteinPosStart());
    virtualMutation.setProteinPosEnd(m.getProteinPosEnd());
    virtualMutation.setKeyword(m.getKeyword());
    virtualMutation.setAlleleSpecificCopyNumber(m.getAlleleSpecificCopyNumber());

    return virtualMutation;
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
    return mutationRepository.getMutationCountByPosition(
        entrezGeneId, proteinPosStart, proteinPosEnd);
  }

  @Override
  public GenomicDataCountItem getMutationCountsByType(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String profileType) {
    // TODO do we need to correct counts for virtual studies?
    return mutationRepository.getMutationCountsByType(
        molecularProfileIds, sampleIds, entrezGeneIds, profileType);
  }
}

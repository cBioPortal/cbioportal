package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.MutationCountByPosition;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.MutationRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;

public class VSAwareMutationRepository implements MutationRepository {

  private final VirtualizationService virtualizationService;
  private final MutationRepository mutationRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareMutationRepository(
      VirtualizationService virtualizationService,
      MutationRepository mutationRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualizationService = virtualizationService;
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
    var resultStream =
        virtualizationService
            .handleMolecularData(
                molecularProfileIds,
                sampleIds,
                Mutation::getMolecularProfileId,
                Mutation::getSampleId,
                (molecularProfileId, sampleId) ->
                    mutationRepository.getMutationsInMultipleMolecularProfiles(
                        molecularProfileId,
                        sampleId,
                        entrezGeneIds,
                        projection,
                        null,
                        null,
                        null,
                        null),
                this::virtualizeMutation)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<Mutation> composeComparator(String sortBy, String direction) {
    MutationSortBy s = MutationSortBy.valueOf(sortBy);
    Comparator<Mutation> result =
        switch (s) {
          case entrezGeneId -> Comparator.comparing(Mutation::getEntrezGeneId);
          case center -> Comparator.comparing(Mutation::getCenter);
          case mutationStatus -> Comparator.comparing(Mutation::getMutationStatus);
          case validationStatus -> Comparator.comparing(Mutation::getValidationStatus);
          case tumorAltCount -> Comparator.comparing(Mutation::getTumorAltCount);
          case tumorRefCount -> Comparator.comparing(Mutation::getTumorRefCount);
          case normalAltCount -> Comparator.comparing(Mutation::getNormalAltCount);
          case normalRefCount -> Comparator.comparing(Mutation::getNormalRefCount);
          case aminoAcidChange -> Comparator.comparing(Mutation::getAminoAcidChange);
          case startPosition -> Comparator.comparing(Mutation::getStartPosition);
          case endPosition -> Comparator.comparing(Mutation::getEndPosition);
          case referenceAllele -> Comparator.comparing(Mutation::getReferenceAllele);
          case variantAllele -> Comparator.comparing(Mutation::getTumorSeqAllele);
          case proteinChange -> Comparator.comparing(Mutation::getProteinChange);
          case mutationType -> Comparator.comparing(Mutation::getMutationType);
          case ncbiBuild -> Comparator.comparing(Mutation::getNcbiBuild);
          case variantType -> Comparator.comparing(Mutation::getVariantType);
          case refseqMrnaId -> Comparator.comparing(Mutation::getRefseqMrnaId);
          case proteinPosStart -> Comparator.comparing(Mutation::getProteinPosStart);
          case proteinPosEnd -> Comparator.comparing(Mutation::getProteinPosEnd);
          case keyword -> Comparator.comparing(Mutation::getKeyword);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
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

    var resultStream =
        virtualizationService
            .handleMolecularData(
                molecularProfileIds,
                sampleIds,
                Mutation::getMolecularProfileId,
                Mutation::getSampleId,
                (molecularProfileId, sampleId) ->
                    mutationRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(
                        molecularProfileId,
                        sampleId,
                        geneQueries,
                        projection,
                        null,
                        null,
                        null,
                        null),
                this::virtualizeMutation)
            .stream();
    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
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
    var resultStream =
        virtualizationService
            .handleMolecularData(
                molecularProfileId,
                sampleIds,
                Mutation::getMolecularProfileId,
                Mutation::getSampleId,
                (mpids, sids) ->
                    mutationRepository.fetchMutationsInMolecularProfile(
                        mpids, sids, entrezGeneIds, snpOnly, projection, null, null, null, null),
                this::virtualizeMutation)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Mutation virtualizeMutation(MolecularProfile molecularProfile, Mutation m) {
    Mutation virtualMutation = new Mutation();
    virtualMutation.setStudyId(molecularProfile.getCancerStudyIdentifier());
    virtualMutation.setMolecularProfileId(molecularProfile.getStableId());
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
    Pair<List<String>, List<String>> idsLists =
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileIds, sampleIds);
    return mutationRepository.getMutationCountsByType(
        idsLists.getKey(), idsLists.getRight(), entrezGeneIds, profileType);
  }
}

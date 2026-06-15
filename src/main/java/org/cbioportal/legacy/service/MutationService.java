package org.cbioportal.legacy.service;

import java.util.List;
import java.util.function.Consumer;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.MutationCountByPosition;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

public interface MutationService {

  List<Mutation> getMutationsInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws MolecularProfileNotFoundException;

  MutationMeta getMetaMutationsInMolecularProfileBySampleListId(
      String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException;

  List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @SuppressWarnings("java:S107") // streaming overload mirrors the many-arg non-streaming method
  void streamMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction,
      Consumer<Mutation> consumer);

  List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<GeneFilterQuery> geneQueries,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds);

  List<Mutation> fetchMutationsInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws MolecularProfileNotFoundException;

  MutationMeta fetchMetaMutationsInMolecularProfile(
      String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException;

  List<MutationCountByPosition> fetchMutationCountsByPosition(
      List<Integer> entrezGeneIds, List<Integer> proteinPosStarts, List<Integer> proteinPosEnds);

  GenomicDataCountItem getMutationCountsByType(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String profileType);
}

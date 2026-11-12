package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import java.util.*;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.shared.MutationQueryOptions;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseMutationRepository implements MutationRepository {

  private final ClickhouseMutationMapper mapper;
  private final MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;

  public ClickhouseMutationRepository(
      ClickhouseMutationMapper clickhouseMutationMapper,
      MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil) {
    this.mapper = clickhouseMutationMapper;
    this.molecularProfileCaseIdentifierUtil = molecularProfileCaseIdentifierUtil;
  }

  @Override
  public List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      MutationQueryOptions mutationQueryOptions) {

    Integer limit = mutationQueryOptions.pageSize();
    Integer offset =
        PaginationCalculator.offset(
            mutationQueryOptions.pageSize(), mutationQueryOptions.pageNumber());

    Map<String, Set<String>> groupedCases =
        molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(
            molecularProfileIds, sampleIds);

    var projection = mutationQueryOptions.projection();
    return switch (projection) {
      case ID ->
          mapper.getMutationsInMultipleMolecularProfilesId(
              groupedCases,
              entrezGeneIds,
              false,
              mutationQueryOptions.projection().name(),
              "",
              limit,
              offset);
      case SUMMARY ->
          mapper.getSummaryMutationsInMultipleMolecularProfiles(
              groupedCases,
              entrezGeneIds,
              false,
              mutationQueryOptions.projection().name(),
              limit,
              offset,
              mutationQueryOptions.sortBy(),
              mutationQueryOptions.direction().name());
      case DETAILED ->
          mapper.getDetailedMutationsInMultipleMolecularProfiles(
              groupedCases,
              entrezGeneIds,
              false,
              mutationQueryOptions.projection().name(),
              limit,
              offset,
              mutationQueryOptions.sortBy(),
              mutationQueryOptions.direction().name());
      default -> new ArrayList<>();
    };
  }

  @Override
  public MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {
    Map<String, Set<String>> groupedCases =
        molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(
            molecularProfileIds, sampleIds);

    return mapper.getMetaMutationsInMultipleMolecularProfiles(groupedCases, entrezGeneIds, false);
  }

  @Override
  public List<MutationSpectrum> getMutationSpectrums(
      String molecularProfileId, String sampleListId, List<String> sampleIds) {
    return mapper.getMutationSpectrums(molecularProfileId, sampleListId, sampleIds);
  }
}

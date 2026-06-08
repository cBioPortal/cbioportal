package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.session.ResultHandler;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.MutationCountByPosition;
import org.cbioportal.legacy.model.meta.MutationMeta;

public interface MutationMapper {

  List<Mutation> getMutationsBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  MutationMeta getMetaMutationsBySampleListId(
      String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds, boolean snpOnly);

  List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /**
   * Streaming overload of {@link #getMutationsInMultipleMolecularProfiles}: routes to the same SQL
   * statement (honoring the same projection/paging/sort) but passes each mapped row to {@code
   * handler} instead of building a list, so a large result set is never materialized in memory.
   */
  void getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction,
      ResultHandler<Mutation> handler);

  List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      boolean snpOnly,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction,
      List<GeneFilterQuery> geneQueries);

  MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly);

  MutationMeta getMetaMutationsBySampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly);

  MutationCountByPosition getMutationCountByPosition(
      Integer entrezGeneId, Integer proteinPosStart, Integer proteinPosEnd);

  GenomicDataCountItem getMutationCountsByType(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly,
      String profileType);
}

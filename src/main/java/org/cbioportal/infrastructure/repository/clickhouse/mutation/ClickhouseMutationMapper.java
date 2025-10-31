package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import java.util.List;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;

/**
 * Mapper interface for retrieving Mutation data from ClickHouse. This interface provides methods to
 * fetch Mutation counts and Mutation data for molecular profile,samples and entrez Gene Ids.
 */
public interface ClickhouseMutationMapper {

  /**
   * Retrieves mutation with ID projection (minimal data set).
   *
   * <p>Returns only essential identifiers: molecularProfileId, sampleId, patientId, entrezGeneId
   * and studyId.
   *
   * @param molecularProfileIds list of distinct molecularProfile
   * @param sampleIds list of distinct sampleIds
   * @param entrezGeneIds list of entrez gene identifiers to filter by
   * @param snpOnly snpOnly flag indicating whether to restrict results to single nucleotide
   *     polymorphisms (SNPs) only
   * @param projection level of detail for each mutation to return for each mutation (e.g. ID,
   *     SUMMARY, DETAILED)
   * @param sortBy currently hardcoded to be an empty string to fit legacy query approach
   * @param limit limit maximum number of results to return (for pagination)
   * @param offset offset number of results to skip before returning (for pagination)
   * @return list of mutation matching the criteria
   */
  List<Mutation> getMutationsInMultipleMolecularProfilesId(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly, // Currently hardcoded to false due to how the legacy worked
      String projection,
      String sortBy, // Currently hardcoded to " " due to how the legacy worked
      Integer limit,
      Integer offset);

  /**
   * Retrieves mutation with SUMMARY projection (basic data with values).
   *
   * <p>Returns basic mutation information, but without detailed mutation metadata.
   *
   * @param molecularProfileIds list of distinct molecularProfile
   * @param sampleIds list of distinct sampleIds
   * @param entrezGeneIds list of entrez gene identifiers to filter by
   * @param snpOnly snpOnly flag indicating whether to restrict results to single nucleotide
   *     polymorphisms (SNPs) only
   * @param projection level of detail for each mutation to return for each mutation (e.g. ID,
   *     SUMMARY, DETAILED)
   * @param limit limit maximum number of results to return (for pagination)
   * @param offset offset number of results to skip before returning (for pagination)
   * @param sortBy sortBy field name to sort results by
   * @param direction sort direction, typically "ASC" or "DESC"
   * @return list of mutation matching the criteria
   */
  List<Mutation> getSummaryMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly, // Currently hardcoded to false due to how the legacy worked
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /**
   * Retrieves mutation with DETAILED projection (complete data set)
   *
   * <p>Returns complete mutation data including all mutation fields. This projection provides the
   * most comprehensive data but may have higher performance costs due to joins.
   *
   * @param molecularProfileIds list of distinct molecularProfile
   * @param sampleIds list of distinct sampleIds
   * @param entrezGeneIds list of entrez gene identifiers to filter by
   * @param snpOnly snpOnly flag indicating whether to restrict results to single nucleotide
   *     polymorphisms (SNPs) only
   * @param projection level of detail for each mutation to return for each mutation (e.g. ID,
   *     SUMMARY, DETAILED)
   * @param limit limit maximum number of results to return (for pagination)
   * @param offset offset number of results to skip before returning (for pagination)
   * @param sortBy sortBy field name to sort results by
   * @param direction sort direction, typically "ASC" or "DESC"
   * @return list of mutation matching the criteria
   */
  List<Mutation> getDetailedMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly, // Currently hardcoded to false due to how the legacy worked
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /**
   * Retrieves the count of mutation matching the specified criteria.
   *
   * <p>Returns total count and sample count that would be returned by a corresponding data
   * retrieval operation, without actually fetching the data.
   *
   * @param molecularProfileIds list of distinct molecularProfile
   * @param sampleIds list of distinct sampleIds
   * @param entrezGeneIds list of entrez gene identifiers to filter by
   * @param snpOnly snpOnly flag indicating whether to restrict results to single nucleotide
   *     polymorphisms (SNPs) only
   * @return MutationMeta
   */
  MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean snpOnly // Currently hardcoded to false due to how the legacy worked
      );
}

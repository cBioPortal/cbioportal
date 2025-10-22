package org.cbioportal.domain.mutation.usecase;

import java.util.List;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.domain.mutation.util.MutationUtil;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.shared.MutationQueryOptions;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving List of Mutation data
 *
 * <p>This use case encapsulates the business logic for fetching mutation data based on the provided
 * MutationQueryOptions. it routes requests to the appropriate repository method
 *
 * <p>Supported projection types:
 *
 * <ul>
 *   <li><strong>ID</strong> - Returns minimal data with only identifiers
 *   <li><strong>SUMMARY</strong> - Returns basic data
 *   <li><strong>DETAILED</strong> - Returns complete data
 * </ul>
 *
 * @see MutationRepository
 */
@Service
@Profile("clickhouse")
public class FetchAllMutationsInProfileUseCase {
  private final MutationRepository mutationRepository;

  public FetchAllMutationsInProfileUseCase(MutationRepository mutationRepository) {
    this.mutationRepository = mutationRepository;
  }

  /**
   * Executes the use case to retrieve mutation data based on filter criteria and projection type.
   *
   * <p>This method transforms the provided filter into a format suitable for the repository layer.
   * If {@code molecularProfileIds} are directly available in the filter, those are used. Otherwise,
   * molecular profile IDs and sample IDs are extracted from the filter’s sample–molecular
   * identifiers.
   *
   * <p>The {@link MutationQueryOptions} controls how much data is returned and in what form:
   *
   * <ul>
   *   <li>projection – level of detail for each mutation (ID, SUMMARY, or DETAILED)
   *   <li>pageSize – define pagination for the results
   *   <li>sortBy – field name to sort the results by
   *   <li>direction – sort order (ASC or DESC)
   * </ul>
   *
   * @param mutationMultipleStudyFilter filter containing profile, sample, and gene identifiers
   * @param mutationQueryOptions criteria for controlling projection, pagination, and sorting of
   *     results
   * @return list of {@link Mutation} objects matching the given filter and search criteria
   * @see MutationQueryOptions
   * @see MutationMultipleStudyFilter
   */
  public List<Mutation> execute(
      MutationMultipleStudyFilter mutationMultipleStudyFilter,
      MutationQueryOptions mutationQueryOptions) {
    if (mutationMultipleStudyFilter.getMolecularProfileIds() != null) {
      return mutationRepository.getMutationsInMultipleMolecularProfiles(
          mutationMultipleStudyFilter.getMolecularProfileIds(),
          null,
          mutationMultipleStudyFilter.getEntrezGeneIds(),
          mutationQueryOptions);
    }

    List<String> molecularProfileIds =
        MutationUtil.extractMolecularProfileIds(
            mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
    List<String> sampleIds =
        MutationUtil.extractSampleIds(mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
    return mutationRepository.getMutationsInMultipleMolecularProfiles(
        molecularProfileIds,
        sampleIds,
        mutationMultipleStudyFilter.getEntrezGeneIds(),
        mutationQueryOptions);
  }
}

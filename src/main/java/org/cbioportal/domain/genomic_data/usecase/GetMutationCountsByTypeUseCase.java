package org.cbioportal.domain.genomic_data.usecase;

import java.util.List;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving mutation counts by type from the repository. This class encapsulates the
 * business logic for fetching mutation counts based on the provided study view filter context and
 * genomic data filters.
 */
public class GetMutationCountsByTypeUseCase {

  private final GenomicDataRepository repository;

  /**
   * Constructs a {@code GetMutationCountsByTypeUseCase} with the provided repository.
   *
   * @param repository the repository to be used for fetching mutation counts
   */
  public GetMutationCountsByTypeUseCase(GenomicDataRepository repository) {
    this.repository = repository;
  }

  /**
   * Executes the use case to retrieve mutation counts by type.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param genomicDataFilters a list of genomic data filters to apply
   * @return a list of {@link GenomicDataCountItem} representing the mutation counts by type
   */
  public List<GenomicDataCountItem> execute(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
    return repository.getMutationCountsByType(studyViewFilterContext, genomicDataFilters);
  }
}

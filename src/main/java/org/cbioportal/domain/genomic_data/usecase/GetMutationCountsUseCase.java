package org.cbioportal.domain.genomic_data.usecase;

import java.util.Map;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.stereotype.Service;

@Service
/**
 * A use case class responsible for retrieving mutation counts based on the provided study view
 * filter context and genomic data filter. This class acts as an intermediary between the
 * application logic and the data repository, delegating the data retrieval to the {@link
 * GenomicDataRepository}.
 */
public class GetMutationCountsUseCase {
  private final GenomicDataRepository repository;

  /**
   * Constructs a new instance of {@link GetMutationCountsUseCase}.
   *
   * @param repository the repository used to access genomic data. Must not be {@code null}.
   */
  public GetMutationCountsUseCase(GenomicDataRepository repository) {
    this.repository = repository;
  }

  /**
   * Executes the use case to retrieve mutation counts based on the provided study view filter
   * context and genomic data filter.
   *
   * @param studyViewFilterContext the context containing study view filter criteria. Must not be
   *     {@code null}.
   * @param genomicDataFilter the filter to apply to the genomic data for retrieving mutation
   *     counts. Must not be {@code null}.
   * @return a map where the key is a string representing a mutation type and the value is the count
   *     of mutations.
   */
  public Map<String, Integer> execute(
      StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter) {
    return repository.getMutationCounts(studyViewFilterContext, genomicDataFilter);
  }
}

package org.cbioportal.domain.genomic_data.usecase;

import java.util.List;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving genomic data bin counts from the repository. This class encapsulates the
 * business logic for fetching genomic data bin counts based on the provided study view filter
 * context and genomic data filters.
 */
public class GetGenomicDataBinCountsUseCase {

  private final GenomicDataRepository genomicDataRepository;

  /**
   * Constructs a {@code GetGenomicDataBinCountsUseCase} with the provided repository.
   *
   * @param genomicDataRepository the repository to be used for fetching genomic data bin counts
   */
  public GetGenomicDataBinCountsUseCase(GenomicDataRepository genomicDataRepository) {
    this.genomicDataRepository = genomicDataRepository;
  }

  /**
   * Executes the use case to retrieve genomic data bin counts.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param genomicDataFilters a list of genomic data bin filters to apply
   * @return a list of {@link ClinicalDataCount} representing the genomic data bin counts
   */
  public List<ClinicalDataCount> execute(
      StudyViewFilterContext studyViewFilterContext,
      List<GenomicDataBinFilter> genomicDataFilters) {
    return genomicDataRepository.getGenomicDataBinCounts(
        studyViewFilterContext, genomicDataFilters);
  }
}

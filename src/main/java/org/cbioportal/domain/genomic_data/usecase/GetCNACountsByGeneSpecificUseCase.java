package org.cbioportal.domain.genomic_data.usecase;

import java.util.List;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.stereotype.Service;

@Service
/**
 * Use case for retrieving CNA counts by gene-specific data from the repository. This class
 * encapsulates the business logic for fetching CNA counts based on the provided study view filter
 * context and genomic data filters.
 */
public class GetCNACountsByGeneSpecificUseCase {

  private final GenomicDataRepository repository;

  /**
   * Constructs a {@code GetCNACountsByGeneSpecificUseCase} with the provided repository.
   *
   * @param repository the repository to be used for fetching CNA counts by gene-specific data
   */
  public GetCNACountsByGeneSpecificUseCase(GenomicDataRepository repository) {
    this.repository = repository;
  }

  /**
   * Executes the use case to retrieve CNA counts by gene-specific data.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param genomicDataFilters a list of genomic data filters to apply
   * @return a list of {@link GenomicDataCountItem} representing the CNA counts by gene
   */
  public List<GenomicDataCountItem> execute(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
    return repository.getCNACounts(studyViewFilterContext, genomicDataFilters);
  }
}

package org.cbioportal.domain.generic_assay.usecase;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.springframework.stereotype.Service;

@Service
/**
 * A use case class responsible for retrieving binned counts of generic assay data based on the
 * provided filters. This class acts as an intermediary between the application logic and the data
 * repository, delegating the data retrieval to the {@link GenericAssayRepository}.
 */
public class GetGenericAssayDataBinCounts {
  private final GenericAssayRepository genericAssayRepository;

  /**
   * Constructs a new instance of {@link GetGenericAssayDataBinCounts}.
   *
   * @param genericAssayRepository the repository used to access generic assay data. Must not be
   *     {@code null}.
   */
  public GetGenericAssayDataBinCounts(GenericAssayRepository genericAssayRepository) {
    this.genericAssayRepository = genericAssayRepository;
  }

  /**
   * Executes the use case to retrieve binned counts of generic assay data based on the provided
   * filters.
   *
   * @param studyViewFilterContext the context containing study view filter criteria. Must not be
   *     {@code null}.
   * @param genericAssayDataBinFilters a list of filters to apply to the generic assay data for
   *     binning. Must not be {@code null}.
   * @return a list of {@link ClinicalDataCount} objects representing the binned counts of generic
   *     assay data that match the provided filters. The list may be empty if no data matches the
   *     filters.
   */
  public List<ClinicalDataCount> execute(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
    return genericAssayRepository.getGenericAssayDataBinCounts(
        studyViewFilterContext, genericAssayDataBinFilters);
  }
}

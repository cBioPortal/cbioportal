package org.cbioportal.domain.clinical_event.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.springframework.stereotype.Service;

@Service
/**
 * Use case for retrieving counts of different clinical event types. This class interacts with the
 * {@link ClinicalEventRepository} to fetch the required data.
 */
public class GetClinicalEventTypeCountsUseCase {

  private final ClinicalEventRepository clinicalEventRepository;

  /**
   * Constructs a use case for retrieving clinical event type counts.
   *
   * @param clinicalEventRepository The repository used to fetch clinical event type counts.
   */
  public GetClinicalEventTypeCountsUseCase(ClinicalEventRepository clinicalEventRepository) {
    this.clinicalEventRepository = clinicalEventRepository;
  }

  /**
   * Executes the use case to retrieve counts of different clinical event types based on the study
   * view filter context.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @return A list of {@link ClinicalEventTypeCount} representing the counts of clinical event
   *     types.
   */
  public List<ClinicalEventTypeCount> execute(StudyViewFilterContext studyViewFilterContext) {
    return this.clinicalEventRepository.getClinicalEventTypeCounts(studyViewFilterContext);
  }
}

package org.cbioportal.domain.patient.repository;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.CaseListDataCount;

/**
 * Repository interface for performing operations related to patient data. This interface defines
 * methods for retrieving filtered patient counts and case list data counts.
 */
public interface PatientRepository {

  /**
   * Retrieves the count of filtered patients based on the provided study view filter context.
   *
   * @param studyViewFilterContext the context containing the filter criteria for the study view
   * @return the count of patients that match the filter criteria
   */
  int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext);

  /**
   * Retrieves the case list data counts based on the provided study view filter context.
   *
   * @param studyViewFilterContext the context containing the filter criteria for the study view
   * @return a list of {@link CaseListDataCount} representing the counts of case list data
   */
  List<CaseListDataCount> getCaseListDataCounts(StudyViewFilterContext studyViewFilterContext);
}

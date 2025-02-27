package org.cbioportal.domain.clinical_data.repository;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.shared.enums.ProjectionType;

/** Repository interface for retrieving clinical data related to patients and samples. */
public interface ClinicalDataRepository {

  /**
   * Retrieves clinical data for patients based on the given study view filter context and filtered
   * attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param filteredAttributes A list of attributes to filter the clinical data.
   * @return A list of {@link ClinicalData} representing patient clinical data.
   */
  List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

  List<ClinicalData> getPatientClinicalData(List<String> studyIds, List<String> sampleIds, List<String> filteredAttributes, ProjectionType projectionType);

    /**
     * Retrieves clinical data for samples based on the given study view filter context and filtered* attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param filteredAttributes A list of attributes to filter the clinical data.
   * @return A list of {@link ClinicalData} representing sample clinical data.
   */
  List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

  List<ClinicalData> getSampleClinicalData(List<String> studyIds, List<String> sampleIds, List<String> filteredAttributes, ProjectionType projectionType);

    /**
     * Retrieves counts of clinical data records based on the given study view filter context and* filtered attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param filteredAttributes A list of attributes to filter the clinical data.
   * @return A list of {@link ClinicalDataCountItem} representing clinical data counts.
   */
  List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);
}

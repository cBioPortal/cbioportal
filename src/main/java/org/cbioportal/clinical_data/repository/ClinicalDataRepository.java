package org.cbioportal.clinical_data.repository;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Repository interface for retrieving clinical data related to patients and samples.
 */
public interface ClinicalDataRepository {

    /**
     * Retrieves clinical data for patients based on the given study view filter context and filtered attributes.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param filteredAttributes A list of attributes to filter the clinical data.
     * @return A list of {@link ClinicalData} representing patient clinical data.
     */
    List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

    /**
     * Retrieves clinical data for samples based on the given study view filter context and filtered attributes.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param filteredAttributes A list of attributes to filter the clinical data.
     * @return A list of {@link ClinicalData} representing sample clinical data.
     */
    List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

    /**
     * Retrieves counts of clinical data records based on the given study view filter context and filtered attributes.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param filteredAttributes A list of attributes to filter the clinical data.
     * @return A list of {@link ClinicalDataCountItem} representing clinical data counts.
     */
    List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext,
                                                      List<String> filteredAttributes);
}

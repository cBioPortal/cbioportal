package org.cbioportal.clinical_data.usecase;

import org.cbioportal.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving clinical data for a patient from the repository.
 * This class encapsulates the business logic for fetching clinical data based on
 * the provided study view filter context and filtered attributes.
 */
public class GetPatientClinicalDataUseCase {

    private final ClinicalDataRepository clinicalDataRepository;

    /**
     * Constructs a {@code GetPatientClinicalDataUseCase} with the provided repository.
     *
     * @param clinicalDataRepository the repository to be used for fetching patient clinical data
     */
    public GetPatientClinicalDataUseCase(ClinicalDataRepository clinicalDataRepository) {
        this.clinicalDataRepository = clinicalDataRepository;
    }

    /**
     * Executes the use case to retrieve clinical data for a patient.
     *
     * @param studyViewFilterContext the context of the study view filter to apply
     * @param filteredAttributes a list of attributes to filter the clinical data
     * @return a list of {@link ClinicalData} representing the patient's clinical data
     */
    public List<ClinicalData> execute(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return clinicalDataRepository.getPatientClinicalData(studyViewFilterContext, filteredAttributes);
    }
}


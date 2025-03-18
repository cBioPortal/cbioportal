package org.cbioportal.domain.patient.usecase;

import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.domain.patient.repository.PatientRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving case list data counts.
 * This class interacts with the {@link PatientRepository} to fetch the case list data counts
 * based on the filter criteria specified in the study view filter context.
 */
public class GetCaseListDataCountsUseCase {

    private final PatientRepository patientRepository;

    /**
     * Constructs a {@code GetCaseListDataCountsUseCase} with the provided {@link PatientRepository}.
     *
     * @param patientRepository the repository to be used for retrieving the case list data counts
     */
    public GetCaseListDataCountsUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Executes the use case to retrieve the case list data counts based on the given study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter to apply
     * @return a list of {@link CaseListDataCount} representing the counts of case list data
     */
    public List<CaseListDataCount> execute(StudyViewFilterContext studyViewFilterContext) {
        return patientRepository.getCaseListDataCounts(studyViewFilterContext);
    }
}


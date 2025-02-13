package org.cbioportal.clinical_event.usecase;

import org.cbioportal.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetClinicalEventTypeCountsUseCase {
    private final ClinicalEventRepository clinicalEventRepository;

    public GetClinicalEventTypeCountsUseCase(ClinicalEventRepository clinicalEventRepository) {
        this.clinicalEventRepository = clinicalEventRepository;
    }

    public List<ClinicalEventTypeCount> execute(StudyViewFilterContext studyViewFilterContext){
        return this.clinicalEventRepository.getClinicalEventTypeCounts(studyViewFilterContext);
    }
}

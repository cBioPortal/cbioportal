package org.cbioportal.clinical_data.usecase;

import org.cbioportal.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetSampleClinicalDataUseCase {
    private final ClinicalDataRepository clinicalDataRepository;

    public GetSampleClinicalDataUseCase(ClinicalDataRepository clinicalDataRepository) {
        this.clinicalDataRepository = clinicalDataRepository;
    }

    public List<ClinicalData> execute(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes){
        return clinicalDataRepository.getSampleClinicalData(studyViewFilterContext, filteredAttributes);
    }
}

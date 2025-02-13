package org.cbioportal.clinical_attributes.usecase;

import org.cbioportal.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetClinicalAttributesForStudiesUseCase {
    private final ClinicalAttributesRepository clinicalAttributesRepository;

    public GetClinicalAttributesForStudiesUseCase(ClinicalAttributesRepository clinicalAttributesRepository) {
        this.clinicalAttributesRepository = clinicalAttributesRepository;
    }

    public List<ClinicalAttribute> execute(List<String> studyIds){
        return clinicalAttributesRepository.getClinicalAttributesForStudies(studyIds);
    }
}

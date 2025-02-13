package org.cbioportal.clinical_attributes.usecase;

import org.cbioportal.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("clickhouse")
public class GetClinicalAttributesDataTypeMapUseCase {

    private final ClinicalAttributesRepository clinicalAttributesRepository;


    public GetClinicalAttributesDataTypeMapUseCase(ClinicalAttributesRepository clinicalAttributesRepository) {
        this.clinicalAttributesRepository = clinicalAttributesRepository;
    }

    public Map<String, ClinicalDataType> execute() {
        return clinicalAttributesRepository.getClinicalAttributeDatatypeMap();
    }
}

package org.cbioportal.clinical_attributes.usecase;

import org.cbioportal.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving a mapping of clinical attribute names to their corresponding data types.
 * This class interacts with the {@link ClinicalAttributesRepository} to fetch the required data.
 */
public class GetClinicalAttributesDataTypeMapUseCase {

    private final ClinicalAttributesRepository clinicalAttributesRepository;


    /**
     * Constructs a use case for retrieving the clinical attribute data type map.
     *
     * @param clinicalAttributesRepository The repository used to fetch clinical attribute data types.
     */
    public GetClinicalAttributesDataTypeMapUseCase(ClinicalAttributesRepository clinicalAttributesRepository) {
        this.clinicalAttributesRepository = clinicalAttributesRepository;
    }


    /**
     * Executes the use case to retrieve a mapping of clinical attribute names to their corresponding data types.
     *
     * @return A map where the key is the clinical attribute name and the value is the corresponding {@link ClinicalDataType}.
     */
    public Map<String, ClinicalDataType> execute() {
        return clinicalAttributesRepository.getClinicalAttributeDatatypeMap();
    }
}

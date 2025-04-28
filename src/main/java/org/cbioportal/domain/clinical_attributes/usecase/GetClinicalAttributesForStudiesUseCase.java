package org.cbioportal.domain.clinical_attributes.usecase;

import org.cbioportal.domain.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving clinical attributes for specified studies.
 * This class interacts with the {@link ClinicalAttributesRepository} to fetch the required data.
 */
public class GetClinicalAttributesForStudiesUseCase {

    private final ClinicalAttributesRepository clinicalAttributesRepository;

    /**
     * Constructs a use case for retrieving clinical attributes for studies.
     *
     * @param clinicalAttributesRepository The repository used to fetch clinical attributes.
     */
    public GetClinicalAttributesForStudiesUseCase(ClinicalAttributesRepository clinicalAttributesRepository) {
        this.clinicalAttributesRepository = clinicalAttributesRepository;
    }

    /**
     * Executes the use case to retrieve clinical attributes for the given list of study IDs.
     *
     * @param studyIds A list of study IDs.
     * @return A list of {@link ClinicalAttribute} representing the clinical attributes for the given studies.
     */
    public List<ClinicalAttribute> execute(List<String> studyIds) {
        return clinicalAttributesRepository.getClinicalAttributesForStudies(studyIds);
    }
}


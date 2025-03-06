package org.cbioportal.infrastructure.repository.clinical_attributes;

import org.cbioportal.legacy.model.ClinicalAttribute;

import java.util.List;

/**
 * Mapper interface for retrieving clinical attributes data from DB.
 * This interface provides methods to fetch clinical attributes either globally or for specific studies.
 */
public interface V2ClinicalAttributesMapper {

    /**
     * Retrieves the list of all clinical attributes.
     *
     * @return a list of clinical attributes
     */
    List<ClinicalAttribute> getClinicalAttributes();

    /**
     * Retrieves the list of clinical attributes for the specified study IDs.
     *
     * @param studyIds the list of study IDs to filter by
     * @return a list of clinical attributes for the given studies
     */
    List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds);
}


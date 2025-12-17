package org.cbioportal.domain.clinical_attributes.repository;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;

public interface ClinicalAttributesRepository {

  /**
   * Retrieves a list of clinical attributes for the specified studies.
   *
   * @param studyIds A list of study IDs.
   * @return A list of {@link ClinicalAttribute} representing the clinical attributes for the given
   *     studies.
   */
  List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds);

  /**
   * Retrieves a list of clinical attributes for the specified studies with detailed projection.
   * This method returns all fields including displayName, description, priority, and cancerStudyId.
   *
   * @param studyIds A list of study IDs.
   * @return A list of {@link ClinicalAttribute} with full details for the given studies.
   */
  List<ClinicalAttribute> getClinicalAttributesForStudiesDetailed(List<String> studyIds);

  /**
   * Retrieves a mapping of clinical attribute names to their corresponding data types.
   *
   * @return A map where the key is the clinical attribute name and the value is the corresponding
   *     {@link ClinicalDataType}.
   */
  Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap();
}

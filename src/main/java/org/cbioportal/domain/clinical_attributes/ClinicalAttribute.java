package org.cbioportal.domain.clinical_attributes;

import java.io.Serializable;

// DETAILED projection
public record ClinicalAttribute(
    String attrId,
    String displayName,
    String description,
    String datatype,
    Boolean patientAttribute,
    String priority,
    Integer cancerStudyId,
    String cancerStudyIdentifier)
    implements Serializable {

  // ID projection
  public ClinicalAttribute(
      String attrId, String datatype, Boolean patientAttribute, String cancerStudyIdentifier) {
    this(attrId, null, null, datatype, patientAttribute, null, null, cancerStudyIdentifier);
  }
}

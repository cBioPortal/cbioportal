package org.cbioportal.domain.clinical_data;

import java.io.Serializable;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;

// DETAILED projection
public record ClinicalData(
    Integer internalId,
    String sampleId,
    String patientId,
    String studyId,
    String attrId,
    String attrValue,
    ClinicalAttribute clinicalAttribute)
    implements Serializable {

  // ID projection
  public ClinicalData(
      Integer internalId, String sampleId, String patientId, String studyId, String attrId) {

    this(internalId, sampleId, patientId, studyId, attrId, null, null);
  }

  // SUMMARY projection
  public ClinicalData(
      Integer internalId,
      String sampleId,
      String patientId,
      String studyId,
      String attrId,
      String attrValue) {

    this(internalId, sampleId, patientId, studyId, attrId, attrValue, null);
  }
}

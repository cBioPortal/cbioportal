package org.cbioportal.domain.clinical_attributes;

import java.io.Serializable;

public record ClinicalAttribute(
    String attrId,
    String displayName,
    String description,
    String datatype,
    Boolean patientAttribute,
    String priority,
    Integer cancerStudyId,
    String cancerStudyIdentifier)
    implements Serializable {}

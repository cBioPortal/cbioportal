package org.cbioportal.domain.patient;

import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;

import java.io.Serializable;

public record Patient(
    Integer internalId,
    String stableId,
    Integer cancerStudyId,
    String cancerStudyIdentifier,
    CancerStudyMetadata cancerStudy
) implements Serializable {
    
}

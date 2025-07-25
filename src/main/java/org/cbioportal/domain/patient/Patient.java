package org.cbioportal.domain.patient;

import java.io.Serializable;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;

public record Patient(
    Integer internalId,
    String stableId,
    Integer cancerStudyId,
    String cancerStudyIdentifier,
    CancerStudyMetadata cancerStudy)
    implements Serializable {}

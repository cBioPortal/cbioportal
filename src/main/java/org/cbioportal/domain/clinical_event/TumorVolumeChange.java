package org.cbioportal.domain.clinical_event;

import java.io.Serializable;

public record TumorVolumeChange(String studyId, String patientId, double percentageChange)
    implements Serializable {}

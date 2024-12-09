package org.cbioportal.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record PatientTreatmentReport (int totalPatients, int totalSamples, List<PatientTreatment> patientTreatments) implements Serializable {
    public PatientTreatmentReport(int totalPatients, int totalSamples) {
        this(totalPatients, totalSamples, Collections.emptyList());
    } 
}

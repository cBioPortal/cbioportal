package org.cbioportal.persistence.model;

public record TreatmentRecord(String patientUniqueId, String treatment, int startTime, int stopTime) {
}

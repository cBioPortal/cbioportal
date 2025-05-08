package org.cbioportal.legacy.persistence.model;

public record TreatmentRecord(
    String patientUniqueId, String treatment, int startTime, int stopTime) {}

package org.cbioportal.domain.resource;

public record ResourceTableTab(
    String resourceId, String label, long totalCount, long patientCount, long sampleCount) {}

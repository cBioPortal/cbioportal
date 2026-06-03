package org.cbioportal.domain.resource;

import java.util.Map;

public record ResourceTableRow(
    String studyId,
    String resourceId,
    String resourceDisplayName,
    String resourceType,
    String patientId,
    String sampleId,
    String url,
    String displayName,
    String type,
    int priority,
    Map<String, Object> metadata) {}

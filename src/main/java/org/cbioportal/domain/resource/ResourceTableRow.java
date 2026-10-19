package org.cbioportal.domain.resource;

import java.util.Map;

public record ResourceTableRow(
    /**
     * Identity of the row, stable across requests. The table orders by it last, so a client can
     * name a row in a link rather than relying on its position.
     */
    Long internalId,
    String studyId,
    String resourceId,
    String resourceDisplayName,
    String resourceType,
    String patientId,
    String sampleId,
    String url,
    String displayName,
    Map<String, Object> metadata) {}

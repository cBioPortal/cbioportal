package org.cbioportal.domain.cancerstudy;

public record ResourceDefinition(
    String resourceId,
    String displayName,
    String description,
    String resourceType,
    String priority,
    Boolean openByDefault,
    String cancerStudyIdentifier,
    String customMetaData) {}

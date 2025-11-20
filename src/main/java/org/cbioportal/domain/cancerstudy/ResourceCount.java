package org.cbioportal.domain.cancerstudy;

public record ResourceCount(
    String resourceId,
    String displayName,
    String description,
    String resourceType,
    String priority,
    Boolean openByDefault,
    String cancerStudyIdentifier,
    String customMetaData,
    Integer sampleCount,
    Integer patientCount) {}

package org.cbioportal.domain.cancerstudy;

public record ResourceCount(
    ResourceDefinition resourceDefinition, Integer sampleCount, Integer patientCount) {}

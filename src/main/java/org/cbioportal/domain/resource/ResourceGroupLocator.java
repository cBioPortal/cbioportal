package org.cbioportal.domain.resource;

/**
 * Names one group whose position the groups endpoint reports: a patient under patient grouping, a
 * sample under sample grouping, each qualified by its study since stable ids repeat across studies.
 */
public record ResourceGroupLocator(String studyId, String patientId, String sampleId) {}

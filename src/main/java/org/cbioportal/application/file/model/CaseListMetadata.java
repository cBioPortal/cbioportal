package org.cbioportal.application.file.model;

import java.util.SequencedSet;

public record CaseListMetadata(
    String cancerStudyIdentifier,
    String stableId,
    String name,
    String description,
    SequencedSet<String> samplIds
) implements StudyRelatedMetadata {}
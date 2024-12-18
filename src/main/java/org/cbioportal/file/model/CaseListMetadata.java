package org.cbioportal.file.model;

import java.util.SequencedSet;

public record CaseListMetadata(
    String cancerStudyIdentifier,
    String stableId,
    String name,
    String description,
    SequencedSet<String> samplIds
) implements StudyRelatedMetadata {}
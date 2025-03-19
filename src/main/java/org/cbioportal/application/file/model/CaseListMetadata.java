package org.cbioportal.application.file.model;

import java.util.SequencedSet;

/**
 * Represents metadata for a case list.
 */
public class CaseListMetadata implements StudyRelatedMetadata {
    private String cancerStudyIdentifier;
    private String stableId;
    private String name;
    private String description;
    private SequencedSet<String> sampleIds;

    public CaseListMetadata() {
    }

    public CaseListMetadata(String cancerStudyIdentifier, String stableId, String name, String description, SequencedSet<String> sampleIds) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.sampleIds = sampleIds;
    }

    @Override
    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SequencedSet<String> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(SequencedSet<String> sampleIds) {
        this.sampleIds = sampleIds;
    }
}
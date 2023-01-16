package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * All IDs relevant for binning of clinical (and custom clinical) data
 */
public class BinningIds {
    private List<String> studyIds = new ArrayList<>();
    private List<String> sampleIds = new ArrayList<>();
    private List<String> patientIds = new ArrayList<>();
    private List<String> studyIdsOfPatients = new ArrayList<>();
    private List<String> uniqueSampleKeys = new ArrayList<>();
    private List<String> uniquePatientKeys = new ArrayList<>();
    private List<String> sampleAttributeIds = new ArrayList<>();
    private List<String> patientAttributeIds = new ArrayList<>();
    private List<String> conflictingPatientAttributeIds = new ArrayList<>();

    public BinningIds() {}

    /**
     * Create shallow clone
     */
    public BinningIds(BinningIds toClone) {
        this();
        this.studyIds = new ArrayList<>(toClone.getStudyIds());
        this.sampleIds = new ArrayList<>(toClone.getSampleIds());
        this.patientIds = new ArrayList<>(toClone.getPatientIds());
        this.studyIdsOfPatients = new ArrayList<>(toClone.getStudyIdsOfPatients());
        this.uniqueSampleKeys = new ArrayList<>(toClone.getUniqueSampleKeys());
        this.uniquePatientKeys = new ArrayList<>(toClone.getUniquePatientKeys());
        this.sampleAttributeIds = new ArrayList<>(toClone.getSampleAttributeIds());
        this.patientAttributeIds = new ArrayList<>(toClone.getPatientAttributeIds());
        this.conflictingPatientAttributeIds = new ArrayList<>(toClone.getConflictingPatientAttributeIds());
    }

    /**
     * Convert lists into unmodifiable lists 
     * to prevent unknown side effects
     */
    public void toImmutable() {
        this.studyIds = unmodifiableList(this.getStudyIds());
        this.sampleIds = unmodifiableList(this.getSampleIds());
        this.patientIds = unmodifiableList(this.getPatientIds());
        this.studyIdsOfPatients = unmodifiableList(this.getStudyIdsOfPatients());
        this.uniqueSampleKeys = unmodifiableList(this.getUniqueSampleKeys());
        this.uniquePatientKeys = unmodifiableList(this.getUniquePatientKeys());
        this.sampleAttributeIds = unmodifiableList(this.getSampleAttributeIds());
        this.patientAttributeIds = unmodifiableList(this.getPatientAttributeIds());
        this.conflictingPatientAttributeIds = unmodifiableList(this.getConflictingPatientAttributeIds());
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public List<String> getSampleIds() {
        return sampleIds;
    }

    public List<String> getPatientIds() {
        return patientIds;
    }

    public List<String> getStudyIdsOfPatients() {
        return studyIdsOfPatients;
    }

    public List<String> getUniqueSampleKeys() {
        return uniqueSampleKeys;
    }

    public List<String> getUniquePatientKeys() {
        return uniquePatientKeys;
    }

    public List<String> getSampleAttributeIds() {
        return sampleAttributeIds;
    }

    public List<String> getPatientAttributeIds() {
        return patientAttributeIds;
    }

    public List<String> getConflictingPatientAttributeIds() {
        return conflictingPatientAttributeIds;
    }
}
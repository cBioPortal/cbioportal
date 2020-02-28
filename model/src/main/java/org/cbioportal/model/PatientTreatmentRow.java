package org.cbioportal.model;

import java.util.Set;

public class PatientTreatmentRow {
    private boolean received;
    private String treatment;
    private int count;
    private float frequency;
    private Set<String> samples;
    private Set<String> studies;

    public PatientTreatmentRow() {}

    public PatientTreatmentRow(boolean received, String treatment, int count, Set<String> samples, Set<String> studies) {
        this.received = received;
        this.treatment = treatment;
        this.count = count;
        this.samples = samples;
        this.studies = studies;
    }

    public boolean getReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public Set<String> getSamples() {
        return samples;
    }

    public void setSamples(Set<String> samples) {
        this.samples = samples;
    }

    public Set<String> getStudies() {
        return studies;
    }

    public void setStudies(Set<String> studies) {
        this.studies = studies;
    }

    // Not implementing an actual equals + hash function because
    // it felt misleading to say that rows with the same treatment and time
    // are equal.
    public String calculateKey() {
        return getTreatment() + getReceived();
    }

    public void add(SampleTreatmentRow toAdd) {
        setCount(getCount() + toAdd.getCount());
        getSamples().addAll(toAdd.getSamples());
        getStudies().addAll(toAdd.getStudies());
    }
}

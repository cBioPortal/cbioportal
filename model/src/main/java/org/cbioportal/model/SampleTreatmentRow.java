package org.cbioportal.model;

import java.util.Set;

public class SampleTreatmentRow {
    private TemporalRelation time;
    private String treatment;
    private int count;
    private float frequency;
    private Set<String> samples;
    private Set<String> studies;

    public SampleTreatmentRow() {}

    public SampleTreatmentRow(TemporalRelation time, String treatment, int count, Set<String> samples, Set<String> studies) {
        this.time = time;
        this.treatment = treatment;
        this.count = count;
        this.samples = samples;
        this.studies = studies;
        this.frequency = 1; // TODO this is wrong
    }

    public TemporalRelation getTime() {
        return time;
    }

    public void setTime(TemporalRelation time) {
        this.time = time;
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
    
    public String calculateKey() {
        return getTreatment() + getTime().name();
    }
    
    public void add(SampleTreatmentRow toAdd) {
        setCount(getCount() + toAdd.getCount());
        getSamples().addAll(toAdd.getSamples());
        getStudies().addAll(toAdd.getStudies());
    }
}

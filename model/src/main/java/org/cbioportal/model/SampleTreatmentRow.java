package org.cbioportal.model;

import java.util.Objects;
import java.util.Set;

public class SampleTreatmentRow {
    private TemporalRelation time;
    private String treatment;
    private int count;
    private Set<ClinicalEventSample> samples;

    public SampleTreatmentRow() {}

    public SampleTreatmentRow(TemporalRelation time, String treatment, int count, Set<ClinicalEventSample> samples) {
        this.time = time;
        this.treatment = treatment;
        this.count = count;
        this.samples = samples;
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

    public Set<ClinicalEventSample> getSamples() {
        return samples;
    }

    public void setSamples(Set<ClinicalEventSample> samples) {
        this.samples = samples;
    }
    
    public String key() {
        return getTreatment() + getTime().name();
    }
    
    public void add(SampleTreatmentRow toAdd) {
        getSamples().addAll(toAdd.getSamples());
        setCount(getSamples().size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleTreatmentRow that = (SampleTreatmentRow) o;
        return getCount() == that.getCount() &&
            getTime() == that.getTime() &&
            getTreatment().equals(that.getTreatment()) &&
            Objects.equals(getSamples(), that.getSamples());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getTreatment(), getCount(), getSamples());
    }
}

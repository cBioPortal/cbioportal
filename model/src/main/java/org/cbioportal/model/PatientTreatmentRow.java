package org.cbioportal.model;

import java.util.Objects;
import java.util.Set;

public class PatientTreatmentRow {
    private String treatment;
    private int count;
    private Set<ClinicalEventSample> samples;

    public PatientTreatmentRow() {}

    public PatientTreatmentRow(String treatment, int count, Set<ClinicalEventSample> samples) {
        this.treatment = treatment;
        this.count = count;
        this.samples = samples;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientTreatmentRow that = (PatientTreatmentRow) o;
        return getCount() == that.getCount() &&
            getTreatment().equals(that.getTreatment()) &&
            Objects.equals(getSamples(), that.getSamples());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTreatment(), getCount(), getSamples());
    }

    public void add(SampleTreatmentRow toAdd) {
        setCount(getCount() + toAdd.getCount());
        getSamples().addAll(toAdd.getSamples()); 
    }
}

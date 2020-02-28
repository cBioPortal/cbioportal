package org.cbioportal.model;

public class TreatmentRow {
    private TemporalRelation time;
    private String treatment;
    private int count;
    private float frequency;

    public TreatmentRow() {}

    public TreatmentRow(TemporalRelation time, String treatment, int count) {
        this.time = time;
        this.treatment = treatment;
        this.count = count;
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
    
    
}

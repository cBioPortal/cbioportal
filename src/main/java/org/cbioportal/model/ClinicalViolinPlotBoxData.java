package org.cbioportal.model;

import java.io.Serializable;

public class ClinicalViolinPlotBoxData implements Serializable {
    private double whiskerLower;
    private double whiskerUpper;
    private double median;
    private double q1;
    private double q3;

    @Override
    public String toString() {
        return "ClinicalViolinPlotBoxData{" +
            "whiskerLower=" + whiskerLower +
            ", whiskerUpper=" + whiskerUpper +
            ", median=" + median +
            ", q1=" + q1 +
            ", q3=" + q3 +
            '}';
    }

    public double getWhiskerLower() {
        return whiskerLower;
    }

    public void setWhiskerLower(double whiskerLower) {
        this.whiskerLower = whiskerLower;
    }

    public double getWhiskerUpper() {
        return whiskerUpper;
    }

    public void setWhiskerUpper(double whiskerUpper) {
        this.whiskerUpper = whiskerUpper;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getQ1() {
        return q1;
    }

    public void setQ1(double q1) {
        this.q1 = q1;
    }

    public double getQ3() {
        return q3;
    }

    public void setQ3(double q3) {
        this.q3 = q3;
    }
    
    public ClinicalViolinPlotBoxData limitWhiskers(ClinicalViolinPlotData d) {
        this.setWhiskerLower(Math.max(this.getWhiskerLower(), d.getAxisStart()));
        this.setWhiskerUpper(Math.min(this.getWhiskerUpper(), d.getAxisEnd()));
        return this;
    }
}
